package io.pillopl.library.lending.domain.dailysheet;

import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.Value;

@Value
public class CheckoutsToOverdueSheet {

    List<Tuple2<BookId, PatronId>> checkouts;

    public Stream<PatronBooksEvent.OverdueCheckoutRegistered> toStreamOfEvents() {
        return checkouts
                .toStream()
                .map(this::tupleToEvent);
    }

    private PatronBooksEvent.OverdueCheckoutRegistered tupleToEvent(Tuple2<BookId, PatronId> overdueCheckouts) {
        return PatronBooksEvent.OverdueCheckoutRegistered.now(overdueCheckouts._2, overdueCheckouts._1);
    }


}
