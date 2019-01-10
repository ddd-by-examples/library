package io.pillopl.library.lending.domain.dailysheet

import io.pillopl.library.lending.domain.book.BookId
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronId
import io.vavr.Tuple
import io.vavr.collection.List
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId

class HoldsToExpireSheetTest extends Specification {

    PatronId patronId = anyPatronId()
    PatronId anotherPatronId = anyPatronId()

    BookId bookId = anyBookId()
    BookId anotherBookId = anyBookId()

    LibraryBranchId libraryBranchId = anyBranch()
    LibraryBranchId anotherLibraryBranchId = anyBranch()

    def 'should transform sheet into stream of BookHoldExpired events'() {
        given:
            HoldsToExpireSheet sheet = sheet(patronId, anotherPatronId, bookId, anotherBookId, libraryBranchId, anotherLibraryBranchId)
        expect:
            sheet.toStreamOfEvents().with {

                PatronBooksEvent.BookHoldExpired first = it.get(0) as PatronBooksEvent.BookHoldExpired
                first.patronId == patronId.patronId
                first.bookId == bookId.bookId
                first.libraryBranchId == libraryBranchId.libraryBranchId
                first.eventId != null

                PatronBooksEvent.BookHoldExpired second = it.get(1) as PatronBooksEvent.BookHoldExpired

                second.patronId == anotherPatronId.patronId
                second.bookId == anotherBookId.bookId
                second.libraryBranchId == anotherLibraryBranchId.libraryBranchId
                second.eventId != null
            }
    }

    private HoldsToExpireSheet sheet(PatronId patronId, PatronId anotherPatronId, BookId bookId, BookId anotherBookId, LibraryBranchId libraryBranchId, LibraryBranchId anotherBranchId) {
        new HoldsToExpireSheet(List.of(Tuple.of(bookId, patronId, libraryBranchId), Tuple.of(anotherBookId, anotherPatronId, anotherBranchId)))
    }
}
