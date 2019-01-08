package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.book.BookInformation;
import io.pillopl.library.lending.domain.book.BookType;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronInformation.PatronType;
import io.vavr.control.Option;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

public interface PatronBooksEvent {

    default PatronId patronId() {
        return new PatronId(getPatronId());
    }

    UUID getPatronId();

    @Value
    class PatronCreated implements PatronBooksEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull PatronType patronType;

        public static PatronCreated now(PatronInformation patronInformation) {
            return new PatronCreated(
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    patronInformation.getType());
        }
    }

    @Value
    class BookPlacedOnHold implements PatronBooksEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull BookType bookType;
        @NonNull UUID libraryBranchId;
        @NonNull Instant holdFrom;
        Instant holdTill;

        public static BookPlacedOnHold now(BookInformation book, LibraryBranchId libraryBranchId, PatronInformation patronInformation, HoldDuration holdDuration) {
            return new BookPlacedOnHold(
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
    class BookPlacedOnHoldEvents implements PatronBooksEvent {
        @NonNull UUID patronId;
        @NonNull BookPlacedOnHold bookPlacedOnHold;
        @NonNull Option<MaximumNumberOhHoldsReached> maximumNumberOhHoldsReached;

        public static BookPlacedOnHoldEvents events(PatronInformation patron, BookPlacedOnHold bookPlacedOnHold) {
            return new BookPlacedOnHoldEvents(patron.getPatronId().getPatronId(), bookPlacedOnHold, Option.none());
        }

        public static BookPlacedOnHoldEvents events(PatronInformation patron, BookPlacedOnHold bookPlacedOnHold, MaximumNumberOhHoldsReached maximumNumberOhHoldsReached) {
            return new BookPlacedOnHoldEvents(patron.getPatronId().getPatronId(), bookPlacedOnHold, Option.of(maximumNumberOhHoldsReached));
        }

    }



    @Value
    class MaximumNumberOhHoldsReached implements PatronBooksEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        int numberOfHolds;

        public static MaximumNumberOhHoldsReached now(PatronInformation patronInformation, int numberOfHolds) {
            return new MaximumNumberOhHoldsReached(
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    numberOfHolds);
        }
    }

    @Value
    class BookCollected implements PatronBooksEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull BookType bookType;
        @NonNull UUID libraryBranchId;

        public static BookCollected now(BookInformation book, LibraryBranchId libraryBranchId, PatronId patronId) {
            return new BookCollected(
                    Instant.now(),
                    patronId.getPatronId(),
                    book.getBookId().getBookId(),
                    book.getBookType(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookReturned implements PatronBooksEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull BookType bookType;
        @NonNull UUID libraryBranchId;
    }

    @Value
    class BookHoldFailed implements PatronBooksEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull String reason;
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull UUID libraryBranchId;

        static BookHoldFailed now(Rejection rejection, BookId bookId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookHoldFailed(
                    rejection.getReason().getReason(),
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookCollectingFailed implements PatronBooksEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull String reason;
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull UUID libraryBranchId;

        static BookCollectingFailed now(Rejection rejection, BookId bookId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookCollectingFailed(
                    rejection.getReason().getReason(),
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookHoldCanceled implements PatronBooksEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull UUID libraryBranchId;

        public static BookHoldCanceled now(BookInformation bookInformation, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookHoldCanceled(
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    bookInformation.getBookId().getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookHoldCancelingFailed implements PatronBooksEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull UUID libraryBranchId;

        static BookHoldCancelingFailed now(BookId bookId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new BookHoldCancelingFailed(
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class BookHoldExpired implements PatronBooksEvent {
        @NonNull UUID eventId = UUID.randomUUID();
        @NonNull Instant when;
        @NonNull UUID patronId;
        @NonNull UUID bookId;
        @NonNull UUID libraryBranchId;

        public static BookHoldExpired now(BookId bookId, PatronId patronId, LibraryBranchId libraryBranchId) {
            return new BookHoldExpired(
                    Instant.now(),
                    patronId.getPatronId(),
                    bookId.getBookId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

}



