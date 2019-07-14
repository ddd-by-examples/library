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

class CheckoutsToOverdueSheetTest extends Specification {

    PatronId patronId = anyPatronId()
    PatronId anotherPatronId = anyPatronId()

    BookId bookId = anyBookId()
    BookId anotherBookId = anyBookId()

    LibraryBranchId libraryBranchId = anyBranch()
    LibraryBranchId anotherLibraryBranchId = anyBranch()

    def 'should transform sheet into stream of OverdueCheckoutRegistered events'() {
        given:
            CheckoutsToOverdueSheet sheet = sheet(patronId, anotherPatronId, bookId, anotherBookId, libraryBranchId, anotherLibraryBranchId)
        expect:
            sheet.toStreamOfEvents().with {

                PatronEvent.OverdueCheckoutRegistered first = it.get(0) as PatronEvent.OverdueCheckoutRegistered
                first.patronId == patronId.patronId
                first.bookId == bookId.bookId
                first.libraryBranchId == libraryBranchId.libraryBranchId
                first.eventId != null

                PatronEvent.OverdueCheckoutRegistered second = it.get(1) as PatronEvent.OverdueCheckoutRegistered

                second.patronId == anotherPatronId.patronId
                second.bookId == anotherBookId.bookId
                second.libraryBranchId == anotherLibraryBranchId.libraryBranchId
                second.eventId != null
            }
    }

    private CheckoutsToOverdueSheet sheet(PatronId patronId, PatronId anotherPatronId, BookId bookId, BookId anotherBookId, LibraryBranchId libraryBranchId, LibraryBranchId anotherBranchId) {
        new CheckoutsToOverdueSheet(List.of(
                new OverdueCheckout(bookId, patronId, libraryBranchId),
                new OverdueCheckout(anotherBookId, anotherPatronId, anotherBranchId)))
    }
}
