package io.pillopl.library.lending.patronprofile.model;

import io.pillopl.library.catalogue.BookId;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
public class PatronProfile {

    @NonNull HoldsView holdsView;
    @NonNull CheckoutsView currentCheckouts;

    public Option<Tuple2<BookId, Instant>> findHold(BookId bookId) {
        return
                holdsView
                .getCurrentHolds()
                .toStream()
                .find(hold -> hold._1.equals(bookId));
    }

    public Option<Tuple2<BookId, Instant>> findCheckout(BookId bookId) {
        return
                currentCheckouts
                        .getCurrentCheckouts()
                        .toStream()
                        .find(hold -> hold._1.equals(bookId));
    }


}
