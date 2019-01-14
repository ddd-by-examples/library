package io.pillopl.library.lending.book.model


import io.pillopl.library.lending.library.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Instant

import static BookFixture.bookOnHold
import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookHoldExpired
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId

class BookHoldExpiredTest extends Specification {

    def 'should make book available when hold expired'() {
        given:
            BookOnHold bookOnHold = bookOnHold()
        and:
            LibraryBranchId branchId = anyBranch()
        when:
            AvailableBook availableBook = bookOnHold.handle(bookHoldExpired(bookOnHold, anyPatronId(), branchId))
        then:
            availableBook.bookId == bookOnHold.bookId
            availableBook.libraryBranch == branchId
            availableBook.version == bookOnHold.version
    }

    BookHoldExpired bookHoldExpired(BookOnHold bookOnHold, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new BookHoldExpired(Instant.now(),
                bookOnHold.getBookId().bookId,
                patronId.patronId,
                libraryBranchId.libraryBranchId)
    }


}
