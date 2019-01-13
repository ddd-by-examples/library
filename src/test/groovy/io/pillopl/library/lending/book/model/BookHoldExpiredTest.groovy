package io.pillopl.library.lending.book.model


import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookDSL.aCirculatingBook
import static io.pillopl.library.lending.domain.book.BookDSL.the
import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldExpired
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatron

class BookHoldExpiredTest extends Specification {

    def 'should make book available when hold expired'() {
        given:
        def bookOnHold = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()

        and:
        BookHoldExpired anEvent = the bookOnHold expired()

        when:
        AvailableBook availableBook = the bookOnHold reactsTo anEvent
        then:
        availableBook.bookId == bookOnHold.bookId
        availableBook.libraryBranch == bookOnHold.libraryBranchId
        availableBook.version == bookOnHold.version
    }

}
