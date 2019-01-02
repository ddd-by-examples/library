package io.pillopl.library.lending.domain.book;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent;
import io.pillopl.library.lending.domain.patron.PatronId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
public class CollectedBook {

    @Getter
    private final BookInformation bookInformation;

    @Getter
    private final LibraryBranchId collectedAt;

    @Getter
    private final PatronId byPatron;

    private final Instant collectedTill;

    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    AvailableBook handle(PatronBooksEvent.BookReturnedByPatron bookReturnedByPatron) {
        return new AvailableBook(
                new BookInformation(
                        new BookId(bookReturnedByPatron.getBookId()),
                        bookReturnedByPatron.getBookType()),
                new LibraryBranchId(bookReturnedByPatron.getLibraryBranchId()));
    }



}

