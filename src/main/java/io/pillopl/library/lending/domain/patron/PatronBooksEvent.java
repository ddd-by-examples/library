package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.book.BookId;
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
    class BookPlacedOnHold implements PatronBooksEvent {
        Instant when;
        UUID patronId;
        UUID bookId;
        UUID libraryBranchId;

        static BookPlacedOnHold now(BookId bookId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookPlacedOnHold(
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookCollected implements PatronBooksEvent {
        Instant when;
        UUID patronId;
        UUID bookId;
        UUID libraryBranchId;

        static BookCollected now(BookId bookId, LibraryBranchId libraryBranchId, PatronId patronId) {
            return new BookCollected(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookReturned implements PatronBooksEvent {
        Instant when;
        UUID patronId;
        UUID bookId;
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



