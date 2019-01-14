package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookOnHold;
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

    boolean maximumHoldsAfterHolding(AvailableBook book) {
        return count() + 1 == MAX_NUMBER_OF_HOLDS;
    }
}
