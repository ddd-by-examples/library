package io.pillopl.library.lending.domain.book;

import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollected;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldExpired;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookReturned;
import io.pillopl.library.lending.domain.patron.PatronId;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
public class BookOnHold implements Book {

    @NonNull
    BookInformation bookInformation;

    @NonNull
    LibraryBranchId holdPlacedAt;

    @NonNull
    PatronId byPatron;

    @NonNull
    Instant holdTill;

    @NonNull
    Version version;

    public AvailableBook handle(BookReturned bookReturned) {
        return new AvailableBook(
                bookInformation, new LibraryBranchId(bookReturned.getLibraryBranchId()),
                version);
    }

    public AvailableBook handle(BookHoldExpired bookHoldExpired) {
        return new AvailableBook(
                bookInformation,
                new LibraryBranchId(bookHoldExpired.getLibraryBranchId()),
                version);
    }

    public CollectedBook handle(BookCollected bookCollected) {
        return new CollectedBook(
                bookInformation,
                new LibraryBranchId(bookCollected.getLibraryBranchId()),
                new PatronId(bookCollected.getPatronId()),
                version);
    }

    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    public boolean by(PatronId patronId) {
        return byPatron.equals(patronId);
    }
}

