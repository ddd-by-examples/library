package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookOnHold;
import lombok.NonNull;
import lombok.Value;

import java.util.Set;

@Value
class PatronHolds {

    static int MAX_NUMBER_OF_HOLDS = 5;

    Set<Hold> resourcesOnHold;

    boolean a(@NonNull BookOnHold bookOnHold) {
        Hold hold = new Hold(bookOnHold.getBookId(), bookOnHold.getHoldPlacedAt());
        return resourcesOnHold.contains(hold);
    }

    int count() {
        return resourcesOnHold.size();
    }

    boolean maximumHoldsAfterHolding(AvailableBook book) {
        return count() + 1 == MAX_NUMBER_OF_HOLDS;
    }
}
