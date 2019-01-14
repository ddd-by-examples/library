package io.pillopl.library.lending.book.model;

import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.library.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHold;
import io.pillopl.library.lending.patron.model.PatronId;
import lombok.NonNull;
import lombok.Value;

@Value
public class AvailableBook implements Book {

    @NonNull
    BookInformation bookInformation;

    @NonNull
    LibraryBranchId libraryBranch;

    @NonNull
    Version version;

    public boolean isRestricted() {
        return bookInformation.getBookType().equals(BookType.Restricted);
    }

    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    public BookOnHold handle(BookPlacedOnHold bookPlacedOnHold) {
        return new BookOnHold(
                bookInformation,
                new LibraryBranchId(bookPlacedOnHold.getLibraryBranchId()),
                new PatronId(bookPlacedOnHold.getPatronId()),
                bookPlacedOnHold.getHoldTill(),
                version);
    }
}

