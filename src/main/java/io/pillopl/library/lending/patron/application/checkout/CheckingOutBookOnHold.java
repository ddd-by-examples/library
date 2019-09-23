package io.pillopl.library.lending.patron.application.checkout;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.patron.model.Patron;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckingOutFailed;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;
import io.vavr.control.Either;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.NonNull;


import static io.pillopl.library.commons.commands.Result.Rejection;
import static io.pillopl.library.commons.commands.Result.Success;
import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.Patterns.$Left;
import static io.vavr.Patterns.$Right;

@AllArgsConstructor
public class CheckingOutBookOnHold {

    private final FindBookOnHold findBookOnHold;
    private final Patrons patronRepository;

    public Try<Result> checkOut(@NonNull CheckOutBookCommand command) {
        return Try.of(() -> {
            BookOnHold bookOnHold = find(command.getBookId(), command.getPatronId());
            Patron patron = find(command.getPatronId());
            Either<BookCheckingOutFailed, BookCheckedOut> result = patron.checkOut(bookOnHold, command.getCheckoutDuration());
            return Match(result).of(
                    Case($Left($()), this::publishEvents),
                    Case($Right($()), this::publishEvents));
        });
    }

    private Result publishEvents(BookCheckedOut bookCheckedOut) {
        patronRepository
                .publish(bookCheckedOut);
        return Success;
    }

    private Result publishEvents(BookCheckingOutFailed bookCheckingOutFailed) {
        patronRepository
                .publish(bookCheckingOutFailed);
        return Rejection;
    }

    private BookOnHold find(BookId id, PatronId patronId) {
        return findBookOnHold
                .findBookOnHold(id, patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Cannot find book on hold with Id: " + id.getBookId()));
    }

    private Patron find(PatronId patronId) {
        return patronRepository
                .findBy(patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Patron with given Id does not exists: " + patronId.getPatronId()));
    }

}
