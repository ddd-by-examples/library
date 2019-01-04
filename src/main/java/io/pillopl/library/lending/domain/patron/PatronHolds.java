package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.book.BookOnHold;
import lombok.Value;

import java.util.Set;

@Value
public class PatronHolds {

    static int MAX_NUMBER_OF_HOLDS = 5;

    Set<PatronHold> resourcesOnHold;

    boolean a(BookOnHold bookOnHold) {
        PatronHold hold = new PatronHold(bookOnHold.getBookId(), bookOnHold.getHoldPlacedAt());
        return resourcesOnHold.contains(hold);
    }

    int count() {
        return resourcesOnHold.size();
    }


    boolean maximumNumberOfHoldsReached() {
        return count() + 1 == MAX_NUMBER_OF_HOLDS;
    }
}
