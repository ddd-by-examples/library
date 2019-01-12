package io.pillopl.library.lending.domain.book;

import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent;
import io.pillopl.library.lending.domain.patron.PatronId;
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

    AvailableBook handle(PatronBooksEvent.BookReturned bookReturnedByPatron) {
        return new AvailableBook(
                bookInformation,
                new LibraryBranchId(bookReturnedByPatron.getLibraryBranchId()),
                version);
    }



}

