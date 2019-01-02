package io.pillopl.library.lending.domain.book

import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronId
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.*
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookReturned
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollected

import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId

class BookReactingToPatronLendingEventsTest extends Specification {

    def 'should return book which is marked as placed on hold in the system'() {
        given:
            BookOnHold onHold = bookOnHold()
        and:
            LibraryBranchId libraryBranchId = anyBranch()
        and:
            PatronId returnBy = anyPatronId()
        when:
            AvailableBook available = onHold.handle(bookReturned(onHold, returnBy, libraryBranchId))
        then:
            available.bookId == onHold.bookId
            available.libraryBranch == libraryBranchId
    }

    def 'should place on hold book which is marked as available in the system'() {
        given:
            AvailableBook available = circulatingAvailableBook()
        and:
            Instant from = Instant.MIN
            Instant till = from.plusSeconds(3600)
        and:
            PatronId onHoldByPatron = anyPatronId()
        and:
            LibraryBranchId libraryBranchId = anyBranch()
        when:
            BookOnHold onHold = available.handle(bookPlacedOnHold(available, onHoldByPatron, libraryBranchId, from, till))
        then:
            onHold.bookId == available.bookId
            onHold.byPatron == onHoldByPatron
            onHold.holdTill == till
            onHold.holdPlacedAt == libraryBranchId
    }

    def 'should return book which is marked as collected in the system'() {
        given:
            CollectedBook collected = collectedBook()
        and:
            PatronId returnedBy = anyPatronId()
        and:
            LibraryBranchId returnedAt = anyBranch()
        when:
            AvailableBook available = collected.handle(bookReturned(collected, returnedBy, returnedAt))
        then:
            available.bookId == collected.bookId
    }

    def 'should collect book which is marked as placed on hold in the system'() {
        given:
            BookOnHold onHold = bookOnHold()
        and:
            PatronId collectedBy = anyPatronId()
        and:
            LibraryBranchId collectedAt = anyBranch()
        when:
            CollectedBook collectedBook = onHold.handle(bookCollected(onHold, collectedBy, collectedAt))
        then:
            collectedBook.bookId == onHold.bookId
            collectedBook.collectedAt == collectedAt
    }

    BookReturned bookReturned(CollectedBook bookCollected, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new BookReturned(Instant.now(),
                patronId.patronId,
                bookCollected.getBookId().bookId,
                bookCollected.bookInformation.bookType,
                libraryBranchId.libraryBranchId)
    }

    BookReturned bookReturned(BookOnHold bookOnHold, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new BookReturned(Instant.now(),
                patronId.patronId,
                bookOnHold.getBookId().bookId,
                bookOnHold.bookInformation.bookType,
                libraryBranchId.libraryBranchId)
    }

    BookCollected bookCollected(BookOnHold bookOnHold, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new BookCollected(Instant.now(),
                patronId.patronId,
                bookOnHold.getBookId().bookId,
                bookOnHold.bookInformation.bookType,
                libraryBranchId.libraryBranchId)
    }

    PatronBooksEvent.BookPlacedOnHold bookPlacedOnHold(AvailableBook availableBook, PatronId byPatron, LibraryBranchId libraryBranchId, Instant from, Instant till) {
        return new PatronBooksEvent.BookPlacedOnHold(
                Instant.now(),
                byPatron.patronId,
                availableBook.getBookId().bookId,
                availableBook.bookInformation.bookType,
                libraryBranchId.libraryBranchId,
                from,
                till)
    }

}
