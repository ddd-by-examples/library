package io.pillopl.library.lending.book.model


import io.pillopl.library.lending.library.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronBooksEvent
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookDSL.aCirculatingBook
import static io.pillopl.library.lending.domain.book.BookDSL.an
import static io.pillopl.library.lending.domain.book.BookDSL.the
import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatron
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId

class BookReturningTest extends Specification {

    def 'should return book which is marked as placed on hold in the system'() {
        given:
        def bookOnHold = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
        and:
        LibraryBranchId aBranch = anyBranch()

        when:
        def availableBook = the bookOnHold isReturnedBy anyPatron() at aBranch

        then:
            available.bookId == bookOnHold.bookId
            available.libraryBranch == aBranch
            available.version == bookOnHold.version
    }

    def 'should place on hold book which is marked as available in the system'() {
        given:
        def availableBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()
        and:
        Instant now = Instant.MIN
        Instant oneHour = now.plusSeconds(3600)
        and:
        PatronId aPatron = anyPatronId()
        and:
        LibraryBranchId aBranch = anyBranch()
        when:
        BookOnHold onHold = an availableBook isPlacedOnHoldBy aPatron at aBranch from now till oneHour
        then:
        onHold.bookId == availableBook.bookId
        onHold.byPatron == aPatron
        onHold.holdTill == oneHour
        onHold.holdPlacedAt == aBranch
    }

    def 'should return book which is marked as collected in the system'() {
        given:
        def collectedBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() collectedBy anyPatron()

        when:
        AvailableBook available = the collectedBook isReturnedBy anyPatron() at anyBranch()

        then:
        available.bookId == collectedBook.bookId
    }

    def 'should collect book which is marked as placed on hold in the system'() {
        given:
        def onHoldBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
        and:
        LibraryBranchId aBranch = anyBranch()
        when:
        CollectedBook collectedBook = an onHoldBook isCollectedBy anyPatron() at aBranch
        then:
        collectedBook.bookId == onHoldBook.bookId
        collectedBook.collectedAt == aBranch
    }

}
