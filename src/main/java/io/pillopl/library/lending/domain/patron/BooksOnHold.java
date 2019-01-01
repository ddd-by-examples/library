package io.pillopl.library.lending.domain.patron;

import lombok.Value;

import java.util.Set;

@Value
//TODO add not null
public class BooksOnHold {

    Set<BookOnHold> resourcesOnHold;

    boolean doesNotContain(BookOnHold bookOnHold) {
        return !resourcesOnHold.contains(bookOnHold);
    }

    int count() {
        return resourcesOnHold.size();
    }

}
