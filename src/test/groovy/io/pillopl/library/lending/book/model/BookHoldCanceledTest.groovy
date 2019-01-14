package io.pillopl.library.lending.book.model


import io.pillopl.library.lending.library.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronBooksEvent
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Instant

import static BookFixture.bookOnHold
import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId

class BookHoldCanceledTest extends Specification {

    def 'should make book available when hold canceled'() {
        given:
            BookOnHold bookOnHold = bookOnHold()
        and:
            LibraryBranchId branchId = anyBranch()
        when:
            AvailableBook availableBook = bookOnHold.handle(bookHoldCanceled(bookOnHold, anyPatronId(), branchId))
        then:
            availableBook.bookId == bookOnHold.bookId
            availableBook.libraryBranch == branchId
            availableBook.version == bookOnHold.version
    }

    PatronBooksEvent.BookHoldCanceled bookHoldCanceled(BookOnHold bookOnHold, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new PatronBooksEvent.BookHoldCanceled(Instant.now(),
                bookOnHold.getBookId().bookId,
                patronId.patronId,
                libraryBranchId.libraryBranchId)
    }


}
