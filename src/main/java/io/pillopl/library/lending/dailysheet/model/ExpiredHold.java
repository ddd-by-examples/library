package io.pillopl.library.lending.dailysheet.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExpired;
import io.pillopl.library.lending.patron.model.PatronId;
import lombok.Value;

@Value
public class ExpiredHold {
    private final BookId heldBook;
    private final PatronId patron;
    private final LibraryBranchId library;

    BookHoldExpired toEvent() {
        return BookHoldExpired.now(this.heldBook, this.patron, this.library);
    }
}
