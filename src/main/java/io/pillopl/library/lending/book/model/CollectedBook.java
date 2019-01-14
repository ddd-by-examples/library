package io.pillopl.library.lending.book.model;

import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.library.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronBooksEvent;
import io.pillopl.library.lending.patron.model.PatronId;
import lombok.NonNull;
import lombok.Value;

@Value
public class CollectedBook implements Book {

    @NonNull
    BookInformation bookInformation;

    @NonNull
    LibraryBranchId collectedAt;

    @NonNull
    PatronId byPatron;

    @NonNull
    Version version;

    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    public AvailableBook handle(PatronBooksEvent.BookReturned bookReturnedByPatron) {
        return new AvailableBook(
                bookInformation,
                new LibraryBranchId(bookReturnedByPatron.getLibraryBranchId()),
                version);
    }



}

