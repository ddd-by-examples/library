package io.pillopl.library.lending.application.expiredhold;


import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldExpired;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.vavr.Tuple3;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.Value;

@FunctionalInterface
public interface FindExpiredHolds{

    ExpiredHolds allExpiredHolds();

    @Value
    class ExpiredHolds {

        List<Tuple3<BookId, PatronId, LibraryBranchId>> expiredHolds;

        Stream<BookHoldExpired> toStreamOfEvents() {
            return expiredHolds
                    .toStream()
                    .map(this::toEvent);
        }

        private BookHoldExpired toEvent(Tuple3<BookId, PatronId, LibraryBranchId> expiredHold) {
            return BookHoldExpired.now(expiredHold._1, expiredHold._2, expiredHold._3);
        }


    }

}
