package io.pillopl.library.lending.domain.dailysheet;

import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.vavr.Tuple3;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.Value;

@Value
public class HoldsToExpireSheet {

    List<Tuple3<BookId, PatronId, LibraryBranchId>> expiredHolds;

    public Stream<PatronBooksEvent.BookHoldExpired> toStreamOfEvents() {
        return expiredHolds
                .toStream()
                .map(this::tupleToEvent);
    }

    public int count() {
        return expiredHolds.size();
    }

    private PatronBooksEvent.BookHoldExpired tupleToEvent(Tuple3<BookId, PatronId, LibraryBranchId> expiredHold) {
        return PatronBooksEvent.BookHoldExpired.now(expiredHold._1, expiredHold._2, expiredHold._3);
    }


}
