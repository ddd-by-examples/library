package io.pillopl.library.lending.application.checkout;


import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.OverdueCheckoutRegistered;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.Value;

@FunctionalInterface
public interface FindCheckoutsToOverduePolicy {

    CheckoutsToMarkAsOverdueView allCheckoutsToOverdue();

    @Value
    class CheckoutsToMarkAsOverdueView {

        List<Tuple2<BookId, PatronId>> checkouts;

        Stream<OverdueCheckoutRegistered> toStreamOfEvents() {
            return checkouts
                    .toStream()
                    .map(this::recordToEvent);
        }

        private OverdueCheckoutRegistered recordToEvent(Tuple2<BookId, PatronId> overdueCheckouts) {
            return OverdueCheckoutRegistered.now(overdueCheckouts._2, overdueCheckouts._1);
        }


    }

}
