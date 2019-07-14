package io.pillopl.library.lending.patronprofile.web;


import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.patron.application.hold.CancelHoldCommand;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patronprofile.model.PatronProfiles;
import io.vavr.Predicates;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static java.util.stream.Collectors.toList;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@AllArgsConstructor
class PatronProfileController {

    private final PatronProfiles patronProfiles;
    private final CancelingHold cancelingHold;

    @GetMapping("/profiles/{patronId}")
    ResponseEntity<EntityModel<ProfileResource>> patronProfile(@PathVariable UUID patronId) {
        return ok(new EntityModel<>(new ProfileResource(patronId)));
    }

    @GetMapping("/profiles/{patronId}/holds/")
    ResponseEntity<CollectionModel<EntityModel<Hold>>> findHolds(@PathVariable UUID patronId) {
        List<EntityModel<Hold>> holds = patronProfiles.fetchFor(new PatronId(patronId))
                .getHoldsView()
                .getCurrentHolds()
                .toStream()
                .map(hold -> resourceWithLinkToHoldSelf(patronId, hold))
                .collect(toList());
        return ResponseEntity.ok(new CollectionModel<>(holds, linkTo(methodOn(PatronProfileController.class).findHolds(patronId)).withSelfRel()));

    }

    @GetMapping("/profiles/{patronId}/holds/{bookId}")
    ResponseEntity<EntityModel<Hold>> findHold(@PathVariable UUID patronId, @PathVariable UUID bookId) {
        return patronProfiles.fetchFor(new PatronId(patronId))
                .findHold(new BookId(bookId))
                .map(hold -> ok(resourceWithLinkToHoldSelf(patronId, hold)))
                .getOrElse(notFound().build());

    }

    @GetMapping("/profiles/{patronId}/checkouts/")
    ResponseEntity<CollectionModel<EntityModel<Checkout>>> findCheckouts(@PathVariable UUID patronId) {
        List<EntityModel<Checkout>> checkouts = patronProfiles.fetchFor(new PatronId(patronId))
                .getCurrentCheckouts()
                .getCurrentCheckouts()
                .toStream()
                .map(checkout -> resourceWithLinkToCheckoutSelf(patronId, checkout))
                .collect(toList());
        return ResponseEntity.ok(new CollectionModel<>(checkouts, linkTo(methodOn(PatronProfileController.class).findHolds(patronId)).withSelfRel()));
    }

    @GetMapping("/profiles/{patronId}/checkouts/{bookId}")
    ResponseEntity<EntityModel<Checkout>> findCheckout(@PathVariable UUID patronId, @PathVariable UUID bookId) {
        return patronProfiles.fetchFor(new PatronId(patronId))
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

    private EntityModel<Hold> resourceWithLinkToHoldSelf(UUID patronId, io.pillopl.library.lending.patronprofile.model.Hold hold) {
        return new EntityModel<>(
                new Hold(hold),
                linkTo(methodOn(PatronProfileController.class).findHold(patronId, hold.getBook().getBookId()))
                        .withSelfRel()
                        .andAffordance(afford(methodOn(PatronProfileController.class)
                                .cancelHold(patronId, hold.getBook().getBookId()))));
    }

    private EntityModel<Checkout> resourceWithLinkToCheckoutSelf(UUID patronId, io.pillopl.library.lending.patronprofile.model.Checkout checkout) {
        return new EntityModel<>(
                new Checkout(checkout),
                linkTo(methodOn(PatronProfileController.class).findCheckout(patronId, checkout.getBook().getBookId()))
                        .withSelfRel());
    }

}

@Value
class ProfileResource extends RepresentationModel {

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

    Hold(io.pillopl.library.lending.patronprofile.model.Hold hold) {
        this.bookId = hold.getBook().getBookId();
        this.till = hold.getTill();
    }
}

@Value
class Checkout {

    UUID bookId;
    Instant till;

    Checkout(io.pillopl.library.lending.patronprofile.model.Checkout hold) {
        this.bookId = hold.getBook().getBookId();
        this.till = hold.getTill();
    }

}
