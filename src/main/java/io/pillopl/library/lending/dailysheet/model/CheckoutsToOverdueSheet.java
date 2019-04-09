package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.OverdueCheckoutRegistered;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.Tuple3;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.NonNull;
import lombok.Value;

@Value
public class CheckoutsToOverdueSheet {

    @NonNull
    List<Tuple3<BookId, PatronId, LibraryBranchId>> checkouts;

    public Stream<OverdueCheckoutRegistered> toStreamOfEvents() {
        return checkouts
                .toStream()
                .map(this::tupleToEvent);
    }

    public int count() {
        return checkouts.size();
    }

    private OverdueCheckoutRegistered tupleToEvent(Tuple3<BookId, PatronId, LibraryBranchId> overdueCheckouts) {
        return OverdueCheckoutRegistered.now(overdueCheckouts._2, overdueCheckouts._1, overdueCheckouts._3);
    }


}
