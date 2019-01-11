package io.pillopl.library.lending.application.hold;

import io.pillopl.commons.commands.BatchResult;
import io.pillopl.library.lending.domain.dailysheet.DailySheet;
import io.pillopl.library.lending.domain.patron.PatronBooksRepository;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ExpiringHolds {

    private final DailySheet find;
    private final PatronBooksRepository patronBooksRepository;

    public Try<BatchResult> expireHolds() {
        return Try.of(() ->
                find.queryForHoldsToExpireSheet()
                .toStreamOfEvents()
                .map(patronBooksRepository::handle)
                .find(Try::isFailure)
                .map(handleEventError -> BatchResult.SomeFailed)
                .getOrElse(BatchResult.FullSuccess));
    }


}
