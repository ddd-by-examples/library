package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.book.BookInformation;
import io.pillopl.library.lending.domain.book.BookType;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

//TODO add notNull
//TODO add id to events
public interface PatronBooksEvent {

    default PatronId patronId() {
        return new PatronId(getPatronId());
    }

    UUID getPatronId();

    @Value
    class BookPlacedOnHoldByPatron implements PatronBooksEvent {
        Instant when;
        UUID patronId;
        UUID bookId;
        BookType bookType;
        UUID libraryBranchId;
        Instant holdFrom;
        Instant holdTill;

        public static BookPlacedOnHoldByPatron now(BookInformation book, LibraryBranchId libraryBranchId, PatronInformation patronInformation, HoldDuration holdDuration) {
            return new BookPlacedOnHoldByPatron(
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    book.getBookId().getBookId(),
                    book.getBookType(),
                    libraryBranchId.getLibraryBranchId(),
                    holdDuration.getFrom(),
                    holdDuration.getTo().getOrNull());
        }
    }

    @Value
    class BookCollectedByPatron implements PatronBooksEvent {
        Instant when;
        UUID patronId;
        UUID bookId;
        BookType bookType;
        UUID libraryBranchId;

        public static BookCollectedByPatron now(BookInformation book, LibraryBranchId libraryBranchId, PatronId patronId) {
            return new BookCollectedByPatron(
                    Instant.now(),
                    patronId.getPatronId(),
                    book.getBookId().getBookId(),
                    book.getBookType(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookReturnedByPatron implements PatronBooksEvent {
        Instant when;
        UUID patronId;
        UUID bookId;
        BookType bookType;
        UUID libraryBranchId;
    }

    @Value
    class BookHoldFailed implements PatronBooksEvent {
        String reason;
        Instant when;
        UUID patronId;
        UUID bookId;
        UUID libraryBranchId;

        static BookHoldFailed now(Reason reason, BookId bookId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookHoldFailed(
                    reason.getReason(),
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookCollectingFailed implements PatronBooksEvent {
        String reason;
        Instant when;
        UUID patronId;
        UUID bookId;
        UUID libraryBranchId;

        static BookCollectingFailed now(Reason reason, BookId bookId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookCollectingFailed(
                    reason.getReason(),
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }
}



