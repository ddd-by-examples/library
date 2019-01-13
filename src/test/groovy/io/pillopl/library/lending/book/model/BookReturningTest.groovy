package io.pillopl.library.lending.book.model

import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronId
import spock.lang.Specification

import java.time.Instant

import static BookDSL.aCirculatingBook
import static BookDSL.the
import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatron

class BookReturningTest extends Specification {

    def 'should return book which is marked as placed on hold in the system'() {
        given:
        def bookOnHold = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()

        and:
        LibraryBranchId aBranch = anyBranch()

        and:
        PatronBooksEvent.BookReturned anEvent = the bookOnHold isReturnedBy anyPatron() at aBranch

        when:
        AvailableBook availableBook = the bookOnHold reactsTo anEvent

        then:
        availableBook.bookId == bookOnHold.bookId
        availableBook.libraryBranch == aBranch
        availableBook.version == bookOnHold.version
    }

    def 'should place on hold book which is marked as available in the system'() {
        given:
        def availableBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()

        and:
        Instant now = Instant.MIN
        Instant oneHour = now.plusSeconds(3600)

        and:
        PatronId aPatron = anyPatron()

        and:
        LibraryBranchId aBranch = anyBranch()

        and:
        def anEvent = the availableBook isPlacedOnHoldBy aPatron at aBranch from now till oneHour

        when:
        BookOnHold onHold = the availableBook reactsTo anEvent

        then:
        onHold.bookId == availableBook.bookId
        onHold.byPatron == aPatron
        onHold.holdTill == oneHour
        onHold.holdPlacedAt == aBranch
    }

    def 'should return book which is marked as collected in the system'() {
        given:
        def collectedBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() collectedBy anyPatron()

        and:
        PatronBooksEvent.BookReturned anEvent = the collectedBook isReturnedBy anyPatron() at anyBranch()

        when:
        AvailableBook available = the collectedBook reactsTo anEvent

        then:
        available.bookId == collectedBook.bookId
    }

    def 'should collect book which is marked as placed on hold in the system'() {
        given:
        def onHoldBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()

        and:
        LibraryBranchId aBranch = anyBranch()

        and:
        PatronBooksEvent.BookCollected anEvent = the onHoldBook isCollectedBy anyPatron() at aBranch

        when:
        CollectedBook collectedBook = the onHoldBook reactsTo anEvent

        then:
        collectedBook.bookId == onHoldBook.bookId
        collectedBook.collectedAt == aBranch
    }

}
