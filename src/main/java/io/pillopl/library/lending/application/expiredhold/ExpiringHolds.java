package io.pillopl.library.lending.application.expiredhold;

import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldExpired;
import io.pillopl.library.lending.domain.patron.PatronBooksRepository;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.vavr.Tuple3;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExpiringHolds {

    public enum Result {
        Success, Error
    }

    private final FindExpiredHolds find;
    private final PatronBooksRepository patronBooksRepository;

    public Try<Result> expireHolds() {
        return Try.of(() ->
                find
                .allExpiredHolds()
                .stream()
                .map(this::toEvent)
                .map(patronBooksRepository::handle)
                .find(Try::isFailure)
                .map(handleEventError -> Result.Error)
                .getOrElse(Result.Success));
    }

    private BookHoldExpired toEvent(Tuple3<BookId, PatronId, LibraryBranchId> expiredHold) {
        return BookHoldExpired.now(expiredHold._1, expiredHold._2, expiredHold._3);
    }

}
