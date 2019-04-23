package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.commons.commands.BatchResult;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.patron.model.PatronEvent;
import io.pillopl.library.lending.patron.model.Patrons;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExpiringHolds {

    private final DailySheet find;
    private final Patrons patronRepository;

    public Try<BatchResult> expireHolds() {
        return Try.of(() ->
                find.queryForHoldsToExpireSheet()
                .toStreamOfEvents()
                .map(this::publish)
                .find(Try::isFailure)
                .map(handleEventError -> BatchResult.SomeFailed)
                .getOrElse(BatchResult.FullSuccess));
    }

    private Try<Void> publish(PatronEvent.BookHoldExpired event) {
        return Try.run(() -> patronRepository.publish(event));
    }


}
