package io.pillopl.library.lending.book.model

import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Instant

import static BookDSL.aCirculatingBook
import static BookDSL.the
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatron

class BookReturningTest extends Specification {

    private static Instant now = Instant.MIN
    private static Instant oneHour = now.plusSeconds(3600)

    def 'should return book which is marked as placed on hold in the system'() {
        given:
            BookDSL bookOnHold = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()

        and:
            LibraryBranchId aBranch = anyBranch()

        and:
            PatronEvent.BookReturned bookReturnedEvent = the bookOnHold isReturnedBy anyPatron() at aBranch

        when:
            AvailableBook availableBook = the bookOnHold reactsTo bookReturnedEvent

        then:
            availableBook.bookId == bookOnHold.bookId
            availableBook.libraryBranch == aBranch
            availableBook.version == bookOnHold.version
    }

    def 'should place on hold book which is marked as available in the system'() {
        given:
            BookDSL availableBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()

        and:
            PatronId aPatron = anyPatron()

        and:
            LibraryBranchId aBranch = anyBranch()

        and:
            PatronEvent.BookPlacedOnHold bookPlacedOnHoldEvent = the availableBook isPlacedOnHoldBy aPatron at aBranch from now till oneHour

        when:
            BookOnHold onHold = the availableBook reactsTo bookPlacedOnHoldEvent

        then:
            onHold.bookId == availableBook.bookId
            onHold.byPatron == aPatron
            onHold.holdTill == oneHour
            onHold.holdPlacedAt == aBranch
    }

    def 'should return book which is marked as checkedOut in the system'() {
        given:
            BookDSL checkedOutBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() checkedOutBy anyPatron()

        and:
            PatronEvent.BookReturned bookReturnedEvent = the checkedOutBook isReturnedBy anyPatron() at anyBranch()

        when:
            AvailableBook available = the checkedOutBook reactsTo bookReturnedEvent

        then:
            available.bookId == checkedOutBook.bookId
    }

    def 'should check out book which is marked as placed on hold in the system'() {
        given:
            BookDSL onHoldBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()

        and:
            LibraryBranchId aBranch = anyBranch()

        and:
		PatronEvent.BookCheckedOut bookCheckedOutEvent = the onHoldBook isCheckedOutBy anyPatron() at aBranch

        when:
            CheckedOutBook checkedOutBook = the onHoldBook reactsTo bookCheckedOutEvent

        then:
            checkedOutBook.bookId == onHoldBook.bookId
            checkedOutBook.checkedOutAt == aBranch
    }

}
