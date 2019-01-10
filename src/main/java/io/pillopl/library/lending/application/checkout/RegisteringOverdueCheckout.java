package io.pillopl.library.lending.application.checkout;

import io.pillopl.library.lending.domain.dailysheet.DailySheet;
import io.pillopl.library.lending.domain.patron.PatronBooksRepository;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RegisteringOverdueCheckout {

    public enum Result {
        Success, Error
    }

    private final DailySheet find;
    private final PatronBooksRepository patronBooksRepository;

    public Try<Result> registerOverdueCheckouts() {
        return Try.of(() ->
                find.checkoutsToOverdue()
                .toStreamOfEvents()
                .map(patronBooksRepository::handle)
                .find(Try::isFailure)
                .map(handleEventError -> Result.Error)
                .getOrElse(Result.Success));
    }


}
