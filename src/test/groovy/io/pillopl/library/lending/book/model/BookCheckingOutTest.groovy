package io.pillopl.library.lending.book.model

import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import spock.lang.Specification

import static BookDSL.aCirculatingBook
import static BookDSL.the
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatron

class BookCheckingOutTest extends Specification {

    def 'should check out book which is marked as placed on hold in the system'() {
        given:
            BookDSL bookOnHold = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()

        and:
            LibraryBranchId aBranch = anyBranch()

        and:
            BookCheckedOut bookCheckedOutEvent = the bookOnHold isCheckedOutBy anyPatron() at aBranch

        when:
            CheckedOutBook checkedOutBook = the bookOnHold reactsTo bookCheckedOutEvent

        then:
            checkedOutBook.bookId == bookOnHold.bookId
            checkedOutBook.checkedOutAt == aBranch
            checkedOutBook.version == bookOnHold.version
    }
}
