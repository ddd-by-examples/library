package io.pillopl.library.lending.book.model

import io.pillopl.library.lending.domain.library.LibraryBranchId
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookDSL.aCirculatingBook
import static io.pillopl.library.lending.domain.book.BookDSL.the
import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollected
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatron

class BookCollectingTest extends Specification {

    def 'should collect book which is marked as placed on hold in the system'() {
        given:
        def bookOnHold = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()

        and:
        LibraryBranchId aBranch = anyBranch()

        and:
        BookCollected anEvent = the bookOnHold isCollectedBy anyPatron() at aBranch

        when:
        CollectedBook collectedBook = the bookOnHold reactsTo anEvent

        then:
        collectedBook.bookId == bookOnHold.bookId
        collectedBook.collectedAt == aBranch
        collectedBook.version == bookOnHold.version
    }
}
