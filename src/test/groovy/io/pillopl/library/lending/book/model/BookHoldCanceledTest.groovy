package io.pillopl.library.lending.book.model


import io.pillopl.library.lending.patron.model.PatronEvent
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookDSL.aCirculatingBook
import static io.pillopl.library.lending.book.model.BookDSL.the
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatron

class BookHoldCanceledTest extends Specification {

    def 'should make book available when hold canceled'() {
        given:
            BookDSL bookOnHold = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
        and:
            PatronEvent.BookHoldCanceled bookHoldCanceledEvent = the bookOnHold isCancelledBy anyPatron()

        when:
            AvailableBook availableBook = the bookOnHold reactsTo bookHoldCanceledEvent
        then:
            availableBook.bookId == bookOnHold.bookId
            availableBook.libraryBranch == bookOnHold.libraryBranchId
            availableBook.version == bookOnHold.version
    }

}
