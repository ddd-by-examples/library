package io.pillopl.library.lending.patronprofile.web;


import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patron.application.hold.CancelHoldCommand;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patronprofile.model.PatronProfile;
import io.pillopl.library.lending.patronprofile.model.PatronProfiles;
import io.vavr.Predicates;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.Resources;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.vavr.API.*;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.*;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
class PatronProfileController {

    private final PatronProfiles patronProfiles;
    private final CancelingHold cancelingHold;

    @GetMapping("/profiles/{patronId}")
    ResponseEntity<Resource<ProfileResource>> patronProfile(@PathVariable UUID patronId) {
        return ok(new Resource<>(new ProfileResource(patronId)));
    }

    @GetMapping("/profiles/{patronId}/holds/")
    ResponseEntity<Resources<Resource<Hold>>> findHolds(@PathVariable UUID patronId) {
        List<Resource<Hold>> holds = patronProfiles.apply(new PatronId(patronId))
                .getHoldsView()
                .getCurrentHolds()
                .toStream()
                .map(hold -> resourceWithLinkToHoldSelf(patronId, hold))
                .collect(toList());
        return ResponseEntity.ok(new Resources<>(holds, linkTo(methodOn(PatronProfileController.class).findHolds(patronId)).withSelfRel()));

    }

    @GetMapping("/profiles/{patronId}/holds/{bookId}")
    ResponseEntity<Resource<Hold>> findHold(@PathVariable UUID patronId, @PathVariable UUID bookId) {
        return patronProfiles.apply(new PatronId(patronId))
                .findHold(new BookId(bookId))
                .map(hold -> ok(resourceWithLinkToHoldSelf(patronId, hold)))
                .getOrElse(notFound().build());

    }

    @GetMapping("/profiles/{patronId}/checkouts/")
    ResponseEntity<Resources<Resource<Checkout>>> findCheckouts(@PathVariable UUID patronId) {
        List<Resource<Checkout>> checkouts = patronProfiles.apply(new PatronId(patronId))
                .getCurrentCheckouts()
                .getCurrentCheckouts()
                .toStream()
                .map(checkout -> resourceWithLinkToCheckoutSelf(patronId, checkout))
                .collect(toList());
        return ResponseEntity.ok(new Resources<>(checkouts, linkTo(methodOn(PatronProfileController.class).findHolds(patronId)).withSelfRel()));
    }

    @GetMapping("/profiles/{patronId}/checkouts/{bookId}")
    ResponseEntity<Resource<Checkout>> findCheckout(@PathVariable UUID patronId, @PathVariable UUID bookId) {
        return patronProfiles.apply(new PatronId(patronId))
                .findCheckout(new BookId(bookId))
                .map(hold -> ok(resourceWithLinkToCheckoutSelf(patronId, hold)))
                .getOrElse(notFound().build());

    }

    @DeleteMapping("/profiles/{patronId}/holds/{bookId}")
    ResponseEntity cancelHold(@PathVariable UUID patronId, @PathVariable UUID bookId) {
        Try<Result> result = cancelingHold.cancelHold(new CancelHoldCommand(Instant.now(), new PatronId(patronId), new BookId(bookId)));
        return result
                .map(success -> ResponseEntity.noContent().build())
                .recover(r -> Match(r).of(Case($(Predicates.instanceOf(IllegalArgumentException.class)), ResponseEntity.notFound().build())))
                .getOrElse(ResponseEntity.status(500).build());




    }

    private Resource<Hold> resourceWithLinkToHoldSelf(UUID patronId, Tuple2<BookId, Instant> hold) {
        return new Resource<>(new Hold(hold._1.getBookId(), hold._2), linkTo(methodOn(PatronProfileController.class).findHold(patronId, hold._1.getBookId())).withSelfRel()
                .andAffordance(afford(methodOn(PatronProfileController.class).cancelHold(patronId, hold._1.getBookId()))));
    }

    private Resource<Checkout> resourceWithLinkToCheckoutSelf(UUID patronId, Tuple2<BookId, Instant> checkout) {
        return new Resource<>(new Checkout(checkout._1.getBookId(), checkout._2), linkTo(methodOn(PatronProfileController.class).findCheckout(patronId, checkout._1.getBookId())).withSelfRel());
    }


}

@Value
class ProfileResource extends ResourceSupport {

    UUID patronId;

    ProfileResource(UUID patronId) {
        this.patronId = patronId;
        add(linkTo(methodOn(PatronProfileController.class).findHolds(patronId)).withRel("holds"));
        add(linkTo(methodOn(PatronProfileController.class).findCheckouts(patronId)).withRel("checkouts"));
        add(linkTo(methodOn(PatronProfileController.class).patronProfile(patronId)).withSelfRel());

    }

}

@Value
class Hold {

    UUID bookId;
    Instant till;

}

@Value
class Checkout {

    UUID bookId;
    Instant till;

}