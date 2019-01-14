package io.pillopl.library.lending.book.model;

import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.library.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.BookCollected;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.BookHoldCanceled;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.BookHoldExpired;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.BookReturned;
import io.pillopl.library.lending.patron.model.PatronId;
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

    public AvailableBook handle(BookHoldCanceled bookHoldCanceled) {
        return new AvailableBook(
                bookInformation, new LibraryBranchId(bookHoldCanceled.getLibraryBranchId()),
                version);
    }

    public BookId getBookId() {
        return bookInformation.getBookId();
    }

    public boolean by(PatronId patronId) {
        return byPatron.equals(patronId);
    }
}

