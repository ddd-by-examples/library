package io.pillopl.library.lending.application.checkout;

import io.pillopl.library.commons.commands.BatchResult;
import io.pillopl.library.lending.domain.dailysheet.DailySheet;
import io.pillopl.library.lending.domain.patron.PatronBooksRepository;
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
                .map(patronBooksRepository::handle)
                .find(Try::isFailure)
                .map(handleEventError -> BatchResult.SomeFailed)
                .getOrElse(BatchResult.FullSuccess));
    }


}
