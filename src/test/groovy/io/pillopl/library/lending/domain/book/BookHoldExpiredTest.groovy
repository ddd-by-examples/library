package io.pillopl.library.lending.domain.book

import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronId
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.bookOnHold
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldExpired
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId

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
