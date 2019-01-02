package io.pillopl.library.lending.domain.book;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent;
import io.pillopl.library.lending.domain.patron.PatronId;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@AllArgsConstructor
public class BookOnHold {

    @Getter
    private final BookInformation bookInformation;

    @Getter
    private final LibraryBranchId holdPlacedAt;

    @Getter
    private final PatronId byPatron;

    private final Instant holdTill;

    AvailableBook handle(PatronBooksEvent.BookReturnedByPatron bookReturned) {
        return new AvailableBook(new BookInformation(new BookId(bookReturned.getBookId()), bookReturned.getBookType()), new LibraryBranchId(bookReturned.getLibraryBranchId()));
    }

    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    public LibraryBranchId getLibraryBranchId() {
        return holdPlacedAt;
    }
}

