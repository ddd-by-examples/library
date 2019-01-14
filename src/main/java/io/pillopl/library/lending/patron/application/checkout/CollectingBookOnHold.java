package io.pillopl.library.lending.patron.application.checkout;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.book.model.BookId;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.library.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.CheckoutDuration;
import io.pillopl.library.lending.patron.model.PatronBooks;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.BookCollected;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.BookCollectingFailed;
import io.pillopl.library.lending.patron.model.PatronBooksRepository;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;
import static io.vavr.API.*;
import static io.vavr.Patterns.$Left;
import static io.vavr.Patterns.$Right;

@AllArgsConstructor
class CollectingBookOnHold {

    private final FindBookOnHold findBookOnHold;
    private final PatronBooksRepository patronBooksRepository;

    Try<Result> collect(CollectBookCommand command) {
        return Try.of(() -> {
            BookOnHold bookOnHold = find( command.getBookId(), command.getPatronId());
            PatronBooks patronBooks = find(command.getPatronId());
            Either<BookCollectingFailed, BookCollected> result = patronBooks.collect(bookOnHold, command.getCheckoutDuration());
            return Match(result).of(
                    Case($Left($()), this::publishEvents),
                    Case($Right($()), this::saveAndPublishEvents));
        });
    }

    private Result publishEvents(BookCollectingFailed bookCollectingFailed) {
        //TODO publish events
        return Rejection;
    }

    private Result saveAndPublishEvents(BookCollected bookCollected) {
        //TODO publish events
        patronBooksRepository
                .publish(bookCollected);
        return Success;
    }

    private BookOnHold find(BookId id, PatronId patronId) {
        return findBookOnHold
                .findBookOnHold(id, patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Cannot find book on hold with Id: " + id.getBookId()));
    }

    private PatronBooks find(PatronId patronId) {
        return patronBooksRepository
                .findBy(patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Patron with given Id does not exists: " + patronId.getPatronId()));
    }

}

@Value
class CollectBookCommand {
    @NonNull Instant timestamp;
    @NonNull PatronId patronId;
    @NonNull LibraryBranchId libraryId;
    @NonNull BookId bookId;
    @NonNull Integer noOfDays;

    static CollectBookCommand create(PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId, int noOfDays) {
        return new CollectBookCommand(Instant.now(), patronId, libraryBranchId, bookId, noOfDays);
    }

    CheckoutDuration getCheckoutDuration() {
        return CheckoutDuration.forNoOfDays(noOfDays);
    }
}
