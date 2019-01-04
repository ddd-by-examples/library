package io.pillopl.library.lending.domain.book;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents;
import io.pillopl.library.lending.domain.patron.PatronId;
import lombok.NonNull;
import lombok.Value;

@Value
public class AvailableBook implements Book {

    @NonNull
    private final BookInformation bookInformation;

    @NonNull
    private final LibraryBranchId libraryBranch;

    public boolean isRestricted() {
        return bookInformation.getBookType().equals(BookType.Restricted);
    }

    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    public BookOnHold handle(BookPlacedOnHoldEvents bookPlacedOnHold) {
        return new BookOnHold(
                new BookInformation(new BookId(bookPlacedOnHold.getBookPlacedOnHold().getBookId()), bookPlacedOnHold.getBookPlacedOnHold().getBookType()),
                new LibraryBranchId(bookPlacedOnHold.getBookPlacedOnHold().getLibraryBranchId()),
                new PatronId(bookPlacedOnHold.getBookPlacedOnHold().getPatronId()),
                bookPlacedOnHold.getBookPlacedOnHold().getHoldTill());
    }
}

