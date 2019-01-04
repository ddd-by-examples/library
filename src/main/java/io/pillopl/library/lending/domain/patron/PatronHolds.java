package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.book.BookOnHold;
import lombok.Value;

import java.util.Set;

@Value
public class PatronHolds {

    Set<PatronHold> resourcesOnHold;

    boolean has(BookOnHold bookOnHold) {
        PatronHold hold = new PatronHold(bookOnHold.getBookId(), bookOnHold.getHoldPlacedAt());
        return resourcesOnHold.contains(hold);
    }

    int count() {
        return resourcesOnHold.size();
    }

}
