package io.pillopl.library.lending.book.model


import io.pillopl.library.lending.library.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Instant

import static BookFixture.bookOnHold
import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookCollected
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId

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
