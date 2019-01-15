package io.pillopl.library.lending.book.model;

import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.library.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronBooksEvent;
import io.pillopl.library.lending.patron.model.PatronId;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor
public class CollectedBook implements Book {

    @NonNull
    BookInformation bookInformation;

    @NonNull
    LibraryBranchId collectedAt;

    @NonNull
    PatronId byPatron;

    @NonNull
    Version version;

    public CollectedBook(BookId bookId, BookType type, LibraryBranchId libraryBranchId, PatronId patronId, Version version) {
        this(new BookInformation(bookId, type), libraryBranchId, patronId, version);
    }

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

