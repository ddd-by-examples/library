package io.pillopl.library.lending.application.holding;

import io.pillopl.library.lending.domain.book.AvailableBook;
import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.HoldDuration;
import io.pillopl.library.lending.domain.patron.PatronBooks;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldFailed;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldByPatron;
import io.pillopl.library.lending.domain.patron.PatronBooksRepository;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;

import static io.pillopl.library.lending.application.holding.PlacingOnHold.Result.Rejection;
import static io.pillopl.library.lending.application.holding.PlacingOnHold.Result.Success;
import static io.vavr.API.*;
import static io.vavr.Patterns.$Left;
import static io.vavr.Patterns.$Right;

@AllArgsConstructor
public class PlacingOnHold {

    enum Result {
        Success, Rejection
    }

    private final FindAvailableBook findAvailableBook;
    private final PatronBooksRepository patronBooksRepository;

    Try<Result> placeOnHold(PlaceOnHoldCommand command) {
        return Try.ofSupplier(() -> {
            AvailableBook availableBook = find(command.getBookId());
            PatronBooks patronBooks = find(command.getPatronId());
            Either<BookHoldFailed, BookPlacedOnHoldByPatron> result = placeOnHold(availableBook, patronBooks, command);
            return Match(result).of(
                    Case($Left($()), this::publishEvents),
                    Case($Right($()), this::saveAndPublishEvents)
            );
        });
    }

    private Either<BookHoldFailed, BookPlacedOnHoldByPatron> placeOnHold(AvailableBook availableBook, PatronBooks patronBooks, PlaceOnHoldCommand hold) {
        if (hold.isOpenEnded()) {
            return patronBooks.placeOnHold(availableBook, HoldDuration.openEnded());
        } else {
            return patronBooks.placeOnHold(availableBook, HoldDuration.closeEnded(hold.getHoldForDays()));
        }
    }


    private Result publishEvents(BookHoldFailed bookHoldFailed) {
        //TODO publish events
        return Rejection;
    }

    private Result saveAndPublishEvents(BookPlacedOnHoldByPatron placedOnHold) {
        //TODO publish events
        patronBooksRepository.reactTo(placedOnHold);
        return Success;
    }

    private AvailableBook find(BookId id) {
        return findAvailableBook
                .with(id)
                .getOrElseThrow(() -> new IllegalArgumentException("Cannot find available book with Id: " + id.getBookId()));
    }

    private PatronBooks find(PatronId patronId) {
        return patronBooksRepository
                .findBy(patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Patron with given Id does not exists: " + patronId.getPatronId()));
    }
}

//TODO not null and validation
@Value
class PlaceOnHoldCommand {
    Instant timestamp;
    PatronId patronId;
    LibraryBranchId libraryId;
    BookId bookId;
    Integer holdForDays;

    boolean isOpenEnded() {
        return holdForDays == null;
    }

    static PlaceOnHoldCommand closeEnded(PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId, int forDays) {
        return new PlaceOnHoldCommand(Instant.now(), patronId, libraryBranchId, bookId, forDays);
    }

    static PlaceOnHoldCommand openEnded(PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId) {
        return new PlaceOnHoldCommand(Instant.now(), patronId, libraryBranchId, bookId, null);
    }
}
