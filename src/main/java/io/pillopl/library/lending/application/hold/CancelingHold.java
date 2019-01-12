package io.pillopl.library.lending.application.hold;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.book.BookOnHold;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooks;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldCanceled;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldCancelingFailed;
import io.pillopl.library.lending.domain.patron.PatronBooksRepository;
import io.pillopl.library.lending.domain.patron.PatronId;
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
public class CancelingHold {

    private final FindBookOnHold findBookOnHold;
    private final PatronBooksRepository patronBooksRepository;

    Try<Result> cancelHold(CancelHoldCommand command) {
        return Try.of(() -> {
            BookOnHold bookOnHold = find(command.getBookId(), command.getPatronId());
            PatronBooks patronBooks = find(command.getPatronId());
            Either<BookHoldCancelingFailed, BookHoldCanceled> result = patronBooks.cancelHold(bookOnHold);
            return Match(result).of(
                    Case($Left($()), this::publishEvents),
                    Case($Right($()), this::saveAndPublishEvents)
            );
        });
    }

    private Result publishEvents(BookHoldCancelingFailed bookHoldCancelingFailed) {
        //TODO publish events
        return Rejection;
    }

    private Result saveAndPublishEvents(BookHoldCanceled bookHoldCanceled) {
        //TODO publish events
        return patronBooksRepository
                .handle(bookHoldCanceled)
                .map((PatronBooks success) -> Success)
                .getOrElse(Rejection);
    }

    private BookOnHold find(BookId bookId, PatronId patronId) {
        return findBookOnHold
                .findBookOnHold(bookId, patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Cannot find book on hold with Id: " + bookId.getBookId()));
    }

    private PatronBooks find(PatronId patronId) {
        return patronBooksRepository
                .findBy(patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Patron with given Id does not exists: " + patronId.getPatronId()));
    }
}

@Value
class CancelHoldCommand {
    @NonNull Instant timestamp;
    @NonNull PatronId patronId;
    @NonNull LibraryBranchId libraryId;
    @NonNull BookId bookId;

}
