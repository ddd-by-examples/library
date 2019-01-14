package io.pillopl.library.lending.patron.application.checkout;

import io.pillopl.library.commons.commands.BatchResult;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.OverdueCheckoutRegistered;
import io.pillopl.library.lending.patron.model.PatronBooksRepository;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RegisteringOverdueCheckout {

    private final DailySheet find;
    private final PatronBooksRepository patronBooksRepository;

    public Try<BatchResult> registerOverdueCheckouts() {
        return Try.of(() ->
                find.queryForCheckoutsToOverdue()
                .toStreamOfEvents()
                .map(this::publish)
                .find(Try::isFailure)
                .map(handleEventError -> BatchResult.SomeFailed)
                .getOrElse(BatchResult.FullSuccess));
    }

    private Try<Void> publish(OverdueCheckoutRegistered event) {
        return Try.run(() -> patronBooksRepository.publish(event));
    }

}
