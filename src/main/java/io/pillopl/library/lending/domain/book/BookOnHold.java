package io.pillopl.library.lending.domain.book;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollected;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookReturned;
import io.pillopl.library.lending.domain.patron.PatronId;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
public class BookOnHold implements Book {

    @NonNull
    private final BookInformation bookInformation;

    @NonNull
    private final LibraryBranchId holdPlacedAt;

    @NonNull
    private final PatronId byPatron;

    @NonNull
    private final Instant holdTill;

    public AvailableBook handle(BookReturned bookReturned) {
        return new AvailableBook(
                new BookInformation(new BookId(bookReturned.getBookId()), bookReturned.getBookType()),
                new LibraryBranchId(bookReturned.getLibraryBranchId()));
    }

    public CollectedBook handle(BookCollected bookCollected) {
        return new CollectedBook(
                new BookInformation(new BookId(bookCollected.getBookId()), bookCollected.getBookType()),
                new LibraryBranchId(bookCollected.getLibraryBranchId()),
                new PatronId(bookCollected.getPatronId()));
    }

    public BookId getBookId() {
        return bookInformation.getBookId();
    }

}

