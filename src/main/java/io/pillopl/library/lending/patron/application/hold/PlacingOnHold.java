package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.*;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldFailed;
import io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

import static io.pillopl.library.commons.commands.Result.Success;
import static io.vavr.API.*;
import static io.vavr.Patterns.$Left;
import static io.vavr.Patterns.$Right;

@AllArgsConstructor
public class PlacingOnHold {

    private final FindAvailableBook findAvailableBook;
    private final PatronRepository patronRepository;

    Try<Result> placeOnHold(@NonNull PlaceOnHoldCommand command) {
        return Try.of(() -> {
            AvailableBook availableBook = find(command.getBookId());
            Patron patron = find(command.getPatronId());
            Either<BookHoldFailed, BookPlacedOnHoldEvents> result = patron.placeOnHold(availableBook, command.getHoldDuration());
            return Match(result).of(
                    Case($Left($()), this::publishEvents),
                    Case($Right($()), this::publishEvents)
            );
        });
    }

    private Result publishEvents(BookPlacedOnHoldEvents placedOnHold) {
        patronRepository.publish(placedOnHold);
        return Success;
    }

    private Result publishEvents(BookHoldFailed bookHoldFailed) {
        patronRepository.publish(bookHoldFailed);
        return Result.Rejection;
    }

    private AvailableBook find(BookId id) {
        return findAvailableBook
                .findAvailableBookBy(id)
                .getOrElseThrow(() -> new IllegalArgumentException("Cannot find available book with Id: " + id.getBookId()));
    }

    private Patron find(PatronId patronId) {
        return patronRepository
                .findBy(patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Patron with given Id does not exists: " + patronId.getPatronId()));
    }
}

@Value
class PlaceOnHoldCommand {
    @NonNull Instant timestamp;
    @NonNull PatronId patronId;
    @NonNull LibraryBranchId libraryId;
    @NonNull BookId bookId;
    Option<Integer> noOfDays;

    static PlaceOnHoldCommand closeEnded(PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId, int forDays) {
        return new PlaceOnHoldCommand(Instant.now(), patronId, libraryBranchId, bookId, Option.of(forDays));
    }

    static PlaceOnHoldCommand openEnded(PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId) {
        return new PlaceOnHoldCommand(Instant.now(), patronId, libraryBranchId, bookId, Option.none());
    }

    HoldDuration getHoldDuration() {
        return noOfDays
                .map(NumberOfDays::of)
                .map(HoldDuration::closeEnded)
                .getOrElse(HoldDuration.openEnded());
    }
}
