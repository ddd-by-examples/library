package io.pillopl.library.lending.domain.dailysheet;

import io.pillopl.library.lending.domain.patron.PatronBooksEvent.*;

public interface DailySheet {

    CheckoutsToOverdueSheet queryForCheckoutsToOverdue();

    HoldsToExpireSheet queryForHoldsToExpireSheet();

    void handle(BookPlacedOnHold event);

    void handle(BookHoldCanceled event);

    void handle(BookHoldExpired event);

    void handle(BookCollected event);

    void handle(BookReturned event);


}
