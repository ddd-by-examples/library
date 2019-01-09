package io.pillopl.library.lending.application.hold;


import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldExpired;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.vavr.Tuple3;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.Value;

@FunctionalInterface
public interface FindHoldsToExpirePolicy {

    HoldsToExpire allHoldsToExpire();

    @Value
    class HoldsToExpire {

        List<Tuple3<BookId, PatronId, LibraryBranchId>> expiredHolds;

        Stream<BookHoldExpired> toStreamOfEvents() {
            return expiredHolds
                    .toStream()
                    .map(this::recordToEvent);
        }

        public int count() {
            return expiredHolds.size();
        }

        private BookHoldExpired recordToEvent(Tuple3<BookId, PatronId, LibraryBranchId> expiredHold) {
            return BookHoldExpired.now(expiredHold._1, expiredHold._2, expiredHold._3);
        }


    }

}
