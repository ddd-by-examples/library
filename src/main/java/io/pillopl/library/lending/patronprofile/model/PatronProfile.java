package io.pillopl.library.lending.patronprofile.model;

import io.pillopl.library.catalogue.BookId;
import io.vavr.control.Option;
import lombok.NonNull;
import lombok.Value;

@Value
public class PatronProfile {

    @NonNull HoldsView holdsView;
    @NonNull CheckoutsView currentCheckouts;

    public Option<Hold> findHold(BookId bookId) {
        return
                holdsView
                        .getCurrentHolds()
                        .toStream()
                        .find(hold -> hold.getBook().equals(bookId));
    }

    public Option<Checkout> findCheckout(BookId bookId) {
        return
                currentCheckouts
                        .getCurrentCheckouts()
                        .toStream()
                        .find(hold -> hold.getBook().equals(bookId));
    }


}
