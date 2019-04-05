package io.pillopl.library.lending.book.model


import spock.lang.Specification

import static BookDSL.aCirculatingBook
import static BookDSL.the
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExpired
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatron

class BookHoldExpiredTest extends Specification {

    def 'should make book available when hold expired'() {
        given:
            BookDSL bookOnHold = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()

        and:
            BookHoldExpired bookHoldExpiredEvent = the bookOnHold expired()

        when:
            AvailableBook availableBook = the bookOnHold reactsTo bookHoldExpiredEvent
        then:
            availableBook.bookId == bookOnHold.bookId
            availableBook.libraryBranch == bookOnHold.libraryBranchId
            availableBook.version == bookOnHold.version
    }

}
