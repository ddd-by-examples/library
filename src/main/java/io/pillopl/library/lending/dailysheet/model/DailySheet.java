package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.lending.patron.model.PatronEvent.*;

public interface DailySheet {

    CheckoutsToOverdueSheet queryForCheckoutsToOverdue();

    HoldsToExpireSheet queryForHoldsToExpireSheet();

    void handle(BookPlacedOnHold event);

    void handle(BookHoldCanceled event);

    void handle(BookHoldExpired event);

    void handle(BookCheckedOut event);

    void handle(BookReturned event);


}
