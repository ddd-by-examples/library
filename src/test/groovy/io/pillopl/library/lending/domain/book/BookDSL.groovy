package io.pillopl.library.lending.domain.book

import io.pillopl.library.commons.aggregates.Version
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronId

import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.version0
import static io.pillopl.library.lending.domain.book.BookType.Circulating

class BookDSL {
    BookType bookType
    BookId bookId
    LibraryBranchId libraryBranchId
    PatronId patronId
    Closure<Book> bookProvider
    Version version = version0()

    static BookDSL the(BookDSL book) {
        book
    }

    static BookDSL aCirculatingBook() {
        new BookDSL(Circulating)
    }

    BookDSL(BookType type) {
        this.bookType = type
    }

    BookDSL(BookDSL from) {
        this.bookType = from.bookType
        this.bookId = from.bookId
        this.libraryBranchId = from.libraryBranchId
        this.patronId = from.patronId
        this.bookProvider = from.bookProvider
        this.version = from.version
    }

    def with(BookId id) {
        this.bookId = id
        this
    }

    def locatedIn(LibraryBranchId libraryBranch) {
        this.libraryBranchId = libraryBranch
        this
    }

    def placedOnHoldBy(PatronId aPatron) {
        this.patronId = aPatron
        this.bookProvider = { ->
            new BookOnHold(new BookInformation(bookId, bookType), libraryBranchId, patronId, Instant.now(), version0())
        }
        this
    }

    def stillAvailable() {
        bookProvider = { -> new AvailableBook(new BookInformation(bookId, bookType), libraryBranchId, version0()) }
        this
    }

    def collectedBy(PatronId aPatron) {
        bookProvider = { ->
            new CollectedBook(new BookInformation(bookId, bookType), libraryBranchId, aPatron, version0())
        }
        this
    }

    def isReturnedBy(PatronId aPatron) {
        new BookDSL(this) {
            PatronBooksEvent.BookReturned at(LibraryBranchId branchId) {
                bookReturned(bookProvider(), aPatron, branchId)
            }
        }
    }

    def expired() {
        bookHoldExpired(bookProvider(), patronId, libraryBranchId)
    }

    def reactsTo(PatronBooksEvent event) {
        bookProvider().handle(event)
    }

    def isPlacedOnHoldBy(PatronId aPatron) {
        new BookDSL(this) {

            PatronId onHoldPatronId
            LibraryBranchId placeOnHoldBranchId
            Instant onHoldFrom

            {
                onHoldPatronId = aPatron
                onHoldFrom = Instant.now()
            }

            def at(LibraryBranchId branchId) {
                placeOnHoldBranchId = branchId
                this
            }

            def from(Instant from) {
                onHoldFrom = from
                this
            }

            PatronBooksEvent.BookPlacedOnHoldEvents till(Instant till) {
                bookPlacedOnHold(bookProvider(), onHoldPatronId, placeOnHoldBranchId, onHoldFrom, till)
            }
        }
    }

    def isCollectedBy(PatronId aPatron) {
        new BookDSL(this) {
            PatronBooksEvent.BookCollected at(LibraryBranchId branchId) {
                bookCollected(bookProvider(), aPatron, branchId)
            }
        }
    }


    private static PatronBooksEvent.BookReturned bookReturned(Book bookCollected, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new PatronBooksEvent.BookReturned(Instant.now(),
                patronId.patronId,
                bookCollected.getBookId().bookId,
                bookCollected.bookInformation.bookType,
                libraryBranchId.libraryBranchId)
    }

    private static PatronBooksEvent.BookCollected bookCollected(Book bookOnHold, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new PatronBooksEvent.BookCollected(Instant.now(),
                patronId.patronId,
                bookOnHold.getBookId().bookId,
                bookOnHold.bookInformation.bookType,
                libraryBranchId.libraryBranchId,
                Instant.now())
    }

    private static PatronBooksEvent.BookPlacedOnHoldEvents bookPlacedOnHold(Book availableBook, PatronId byPatron, LibraryBranchId libraryBranchId, Instant from, Instant till) {
        return PatronBooksEvent.BookPlacedOnHoldEvents.events(
                new PatronBooksEvent.BookPlacedOnHold(Instant.now(),
                        byPatron.patronId,
                        availableBook.getBookId().bookId,
                        availableBook.bookInformation.bookType,
                        libraryBranchId.libraryBranchId,
                        from,
                        till),
        )
    }

    private static PatronBooksEvent.BookHoldExpired bookHoldExpired(Book bookOnHold, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new PatronBooksEvent.BookHoldExpired(Instant.now(),
                bookOnHold.getBookId().bookId,
                patronId.patronId,
                libraryBranchId.libraryBranchId)
    }

}