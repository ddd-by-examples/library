package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.lending.patron.model.PatronEvent;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.NonNull;
import lombok.Value;
import org.springframework.context.event.EventListener;

@Value
public class HoldsToExpireSheet {

    @NonNull
    List<ExpiredHold> expiredHolds;

    @EventListener
    public Stream<PatronEvent.BookHoldExpired> toStreamOfEvents() {
        return expiredHolds
                .toStream()
                .map(ExpiredHold::toEvent);
    }

    public int count() {
        return expiredHolds.size();
    }

}
