package io.pillopl.library.lending.domain.book

import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronInformation

import java.time.Instant

class BookFixt {
    BookType bookType
    BookId bookId
    LibraryBranchId libraryBranchId
    PatronId patronId
    Closure<Book> provider

    BookFixt(BookType type) {
        this.bookType = type
    }

    BookFixt(BookFixt from) {
        this.bookType = from.bookType
        this.bookId = from.bookId
        this.libraryBranchId = from.libraryBranchId
        this.patronId = from.patronId
        this.provider = from.provider
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
        this.provider = { ->
            new BookOnHold(new BookInformation(bookId, bookType), libraryBranchId, patronId, Instant.now())
        }
        this
    }

    def stillAvailable() {
        provider = { -> new AvailableBook(new BookInformation(bookId, bookType), libraryBranchId) }
        this
    }

    def collectedBy(PatronId aPatron) {
        provider = { -> new CollectedBook(new BookInformation(bookId, bookType), libraryBranchId, aPatron) }
        this
    }

    def isReturnedBy(PatronId aPatron) {
        new BookFixt(this) {
            AvailableBook at(LibraryBranchId branchId) {
                def book = provider()
                book.handle(bookReturned(book, aPatron, branchId))
            }
        }
    }

    def isPlacedOnHoldBy(PatronId aPatron) {
        new BookFixt(this) {

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

            BookOnHold till(Instant till) {
                def book = provider()
                book.handle(bookPlacedOnHold(book, onHoldPatronId, placeOnHoldBranchId, onHoldFrom, till))
            }
        }
    }

    def isCollectedBy(PatronId aPatron) {
        new BookFixt(this) {
            CollectedBook at(LibraryBranchId branchId) {
                def book = provider()
                book.handle(bookCollected(book, aPatron, branchId))
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

}