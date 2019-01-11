package io.pillopl.library.lending.domain.book

import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronId
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.circulatingAvailableBook
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId

class BookPlacingOnHoldTest extends Specification {

    def 'should place on hold book which is marked as available in the system'() {
        given:
            AvailableBook available = circulatingAvailableBook()
        and:
            Instant from = Instant.MIN
            Instant till = from.plusSeconds(3600)
        and:
            PatronId onHoldByPatron = anyPatronId()
        and:
            LibraryBranchId libraryBranchId = anyBranch()
        when:
            BookOnHold onHold = available.handle(bookPlacedOnHold(available, onHoldByPatron, libraryBranchId, from, till))
        then:
            onHold.bookId == available.bookId
            onHold.byPatron == onHoldByPatron
            onHold.holdTill == till
            onHold.holdPlacedAt == libraryBranchId
    }

    PatronBooksEvent.BookPlacedOnHoldEvents bookPlacedOnHold(AvailableBook availableBook, PatronId byPatron, LibraryBranchId libraryBranchId, Instant from, Instant till) {
        return PatronBooksEvent.BookPlacedOnHoldEvents.events(
                new PatronBooksEvent.BookPlacedOnHold(Instant.now(),
                        byPatron.patronId,
                        availableBook.getBookId().bookId,
                        availableBook.bookInformation.bookType,
                        libraryBranchId.libraryBranchId,
                        from,
                        till),
        )
    }

}
