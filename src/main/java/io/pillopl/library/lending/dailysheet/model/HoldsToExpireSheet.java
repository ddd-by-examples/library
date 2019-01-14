package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.lending.book.model.BookId;
import io.pillopl.library.lending.library.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronBooksEvent;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.Tuple3;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

@Value
public class HoldsToExpireSheet {

    List<Tuple3<BookId, PatronId, LibraryBranchId>> expiredHolds;

    @EventListener
    public Stream<PatronBooksEvent.BookHoldExpired> toStreamOfEvents() {
        ApplicationEventPublisher eventPublisher = null;
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
