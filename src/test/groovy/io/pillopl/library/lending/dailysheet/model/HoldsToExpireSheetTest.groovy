package io.pillopl.library.lending.dailysheet.model

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.vavr.collection.List
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId

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

                PatronEvent.BookHoldExpired first = it.get(0) as PatronEvent.BookHoldExpired
                first.patronId == patronId.patronId
                first.bookId == bookId.bookId
                first.libraryBranchId == libraryBranchId.libraryBranchId
                first.eventId != null

                PatronEvent.BookHoldExpired second = it.get(1) as PatronEvent.BookHoldExpired

                second.patronId == anotherPatronId.patronId
                second.bookId == anotherBookId.bookId
                second.libraryBranchId == anotherLibraryBranchId.libraryBranchId
                second.eventId != null
            }
    }

    private HoldsToExpireSheet sheet(PatronId patronId, PatronId anotherPatronId, BookId bookId, BookId anotherBookId, LibraryBranchId libraryBranchId, LibraryBranchId anotherBranchId) {
        new HoldsToExpireSheet(List.of(
                new ExpiredHold(bookId, patronId, libraryBranchId),
                new ExpiredHold(anotherBookId, anotherPatronId, anotherBranchId)))
    }
}
