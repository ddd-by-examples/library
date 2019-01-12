package io.pillopl.library.lending.domain.book

import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronId
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.bookOnHold
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollected
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId

class BookCollectingTest extends Specification {

    def 'should collect book which is marked as placed on hold in the system'() {
        given:
            BookOnHold onHold = bookOnHold()
        and:
            PatronId collectedBy = anyPatronId()
        and:
            LibraryBranchId collectedAt = anyBranch()
        when:
            CollectedBook collectedBook = onHold.handle(bookCollected(onHold, collectedBy, collectedAt))
        then:
            collectedBook.bookId == onHold.bookId
            collectedBook.collectedAt == collectedAt
            collectedBook.version == onHold.version
    }

    BookCollected bookCollected(BookOnHold bookOnHold, PatronId patronId, LibraryBranchId libraryBranchId) {
        return new BookCollected(Instant.now(),
                patronId.patronId,
                bookOnHold.getBookId().bookId,
                bookOnHold.bookInformation.bookType,
                libraryBranchId.libraryBranchId,
                Instant.now())
    }


}
