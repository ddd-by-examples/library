package io.pillopl.library.lending.application.hold;

import io.pillopl.library.lending.domain.dailysheet.DailySheet;
import io.pillopl.library.lending.domain.patron.PatronBooksRepository;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExpiringHolds {

    public enum Result {
        Success, Error
    }

    private final DailySheet find;
    private final PatronBooksRepository patronBooksRepository;

    public Try<Result> expireHolds() {
        return Try.of(() ->
                find.holdsToExpireSheet()
                .toStreamOfEvents()
                .map(patronBooksRepository::handle)
                .find(Try::isFailure)
                .map(handleEventError -> Result.Error)
                .getOrElse(Result.Success));
    }


}
