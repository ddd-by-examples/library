package io.pillopl.library.lending.application.expiredhold;

import io.pillopl.library.lending.domain.patron.PatronBooksRepository;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

import static java.time.Instant.now;

@AllArgsConstructor
public class ExpiringHolds {

    public enum Result {
        Success, Error
    }

    private final FindExpiredHolds find;
    private final PatronBooksRepository patronBooksRepository;

    public Try<Result> expireHolds() {
        return Try.of(() ->
                find.allExpiredHolds()
                .toStreamOfEvents()
                .map(patronBooksRepository::handle)
                .find(Try::isFailure)
                .map(handleEventError -> Result.Error)
                .getOrElse(Result.Success));
    }


}
