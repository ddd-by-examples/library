package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.model.BookId;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.library.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronBooks;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.BookHoldCanceled;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.BookHoldCancelingFailed;
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
public class CancelingHold {

    private final FindBookOnHold findBookOnHold;
    private final PatronBooksRepository patronBooksRepository;

    Try<Result> cancelHold(CancelHoldCommand command) {
        return Try.of(() -> {
            BookOnHold bookOnHold = find(command.getBookId(), command.getPatronId());
            PatronBooks patronBooks = find(command.getPatronId());
            Either<BookHoldCancelingFailed, BookHoldCanceled> result = patronBooks.cancelHold(bookOnHold);
            return Match(result).of(
                    Case($Left($()), bookHoldCancelingFailed -> Rejection),
                    Case($Right($()), this::saveAndPublishEvents)
            );
        });
    }

    private Result saveAndPublishEvents(BookHoldCanceled bookHoldCanceled) {
        patronBooksRepository
                .publish(bookHoldCanceled);
        return Success;
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
