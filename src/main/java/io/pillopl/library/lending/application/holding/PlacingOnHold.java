package io.pillopl.library.lending.application.holding;

import io.pillopl.library.lending.domain.patron.PatronId;
import io.pillopl.library.lending.domain.patron.PatronBooks;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldFailed;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold;
import io.pillopl.library.lending.domain.patron.PatronBooksRepository;
import io.pillopl.library.lending.domain.book.Book;
import io.pillopl.library.lending.domain.book.BookId;
import io.vavr.control.Either;
import lombok.AllArgsConstructor;

import static io.pillopl.library.lending.application.holding.PlacingOnHold.Result.FAILURE;
import static io.pillopl.library.lending.application.holding.PlacingOnHold.Result.SUCCESS;
import static io.vavr.API.*;
import static io.vavr.Patterns.$Left;
import static io.vavr.Patterns.$Right;

@AllArgsConstructor
public class PlacingOnHold {

    enum Result {
        SUCCESS, FAILURE
    }

    private final FindBook findBook;
    private final PatronBooksRepository patronBooksRepository;

    //TODO return TRY?
    public Result placeOnHold(BookId id, PatronId patronId) {
        Book book = findResource(id);
        PatronBooks patronBooks = findPatronCurrentResources(patronId);
        Either<BookHoldFailed, BookPlacedOnHold> result = patronBooks.placeOnHold(book);
        return Match(result).of(
                Case($Left($()), this::publishEvents),
                Case($Right($()), this::saveAndPublishEvents)
        );
    }

    private Result publishEvents(BookHoldFailed bookHoldFailed) {
        //TODO publish events
        return FAILURE;
    }

    private Result saveAndPublishEvents(BookPlacedOnHold placedOnHold) {
        //TODO publish events
        patronBooksRepository.reactTo(placedOnHold);
        return SUCCESS;
    }

    private Book findResource(BookId id) {
        return findBook.with(id)
                .getOrElseThrow(() -> new IllegalArgumentException("Book with given Id does not exists: " + id.getBookId()));
    }

    private PatronBooks findPatronCurrentResources(PatronId patronId) {
        return patronBooksRepository
                .findBy(patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Patron with given Id does not exists: " + patronId.getPatronId()));
    }
}
