package io.pillopl.library.lending.domain.book;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent;
import io.pillopl.library.lending.domain.patron.PatronId;
import lombok.NonNull;
import lombok.Value;

@Value
public class CollectedBook implements Book {

    @NonNull
    private final BookInformation bookInformation;

    @NonNull
    private final LibraryBranchId collectedAt;

    @NonNull
    private final PatronId byPatron;

    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    AvailableBook handle(PatronBooksEvent.BookReturned bookReturnedByPatron) {
        return new AvailableBook(
                new BookInformation(
                        new BookId(bookReturnedByPatron.getBookId()),
                        bookReturnedByPatron.getBookType()),
                new LibraryBranchId(bookReturnedByPatron.getLibraryBranchId()));
    }



}

