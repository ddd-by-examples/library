package io.pillopl.library.lending.book.model


import io.pillopl.library.lending.library.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronBooksEvent
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Instant

import static BookFixture.circulatingAvailableBook
import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId

class BookPlacingOnHoldTest extends Specification {

    def 'should place on hold book which is marked as available in the system'() {
        given:
            AvailableBook book = circulatingAvailableBook()
        and:
            Instant from = Instant.MIN
            Instant till = from.plusSeconds(3600)
        and:
            PatronId onHoldByPatron = anyPatronId()
        and:
            LibraryBranchId libraryBranchId = anyBranch()
        when:
            BookOnHold onHold = book.handle(bookPlacedOnHold(book, onHoldByPatron, libraryBranchId, from, till))
        then:
            onHold.bookId == book.bookId
            onHold.byPatron == onHoldByPatron
            onHold.holdTill == till
            onHold.holdPlacedAt == libraryBranchId
            onHold.version == book.version

    }

    PatronBooksEvent.BookPlacedOnHold bookPlacedOnHold(AvailableBook availableBook, PatronId byPatron, LibraryBranchId libraryBranchId, Instant from, Instant till) {
        return new PatronBooksEvent.BookPlacedOnHold(Instant.now(),
                        byPatron.patronId,
                        availableBook.getBookId().bookId,
                        availableBook.bookInformation.bookType,
                        libraryBranchId.libraryBranchId,
                        from,
                        till)
    }

}
