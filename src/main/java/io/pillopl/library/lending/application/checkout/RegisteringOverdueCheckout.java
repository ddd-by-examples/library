package io.pillopl.library.lending.application.checkout;

import io.pillopl.library.lending.domain.patron.PatronBooksRepository;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RegisteringOverdueCheckout {

    public enum Result {
        Success, Error
    }

    private final FindCheckoutsToOverduePolicy find;
    private final PatronBooksRepository patronBooksRepository;

    public Try<Result> registerOverdueCheckouts() {
        return Try.of(() ->
                find.allCheckoutsToOverdue()
                .toStreamOfEvents()
                .map(patronBooksRepository::handle)
                .find(Try::isFailure)
                .map(handleEventError -> Result.Error)
                .getOrElse(Result.Success));
    }


}
