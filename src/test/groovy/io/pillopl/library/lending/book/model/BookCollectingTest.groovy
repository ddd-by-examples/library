package io.pillopl.library.lending.book.model

import io.pillopl.library.lending.library.model.LibraryBranchId
import spock.lang.Specification

import static BookDSL.aCirculatingBook
import static BookDSL.the
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookCollected
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatron

class BookCollectingTest extends Specification {

    def 'should collect book which is marked as placed on hold in the system'() {
        given:
            BookDSL bookOnHold = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()

        and:
            LibraryBranchId aBranch = anyBranch()

        and:
            BookCollected bookCollectedEvent = the bookOnHold isCollectedBy anyPatron() at aBranch

        when:
            CollectedBook collectedBook = the bookOnHold reactsTo bookCollectedEvent

        then:
            collectedBook.bookId == bookOnHold.bookId
            collectedBook.collectedAt == aBranch
            collectedBook.version == bookOnHold.version
    }
}
