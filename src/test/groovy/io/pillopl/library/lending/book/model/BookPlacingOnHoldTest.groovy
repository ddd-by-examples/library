package io.pillopl.library.lending.book.model

import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookDSL.aCirculatingBook
import static io.pillopl.library.lending.book.model.BookDSL.the
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatron

class BookPlacingOnHoldTest extends Specification {

    private static Instant now = Instant.MIN
    private static Instant oneHourLater = now.plusSeconds(3600)

    def 'should place on hold book which is marked as available in the system'() {
        given:
            BookDSL availableBook = aCirculatingBook() with anyBookId() locatedIn anyBranch() stillAvailable()

        and:
            PatronId aPatron = anyPatron()

        and:
            LibraryBranchId aBranch = anyBranch()

        and:
            PatronEvent.BookPlacedOnHold bookPlacedOnHoldEvent = the availableBook isPlacedOnHoldBy aPatron at aBranch from now till oneHourLater

        when:
            BookOnHold onHold = the availableBook reactsTo bookPlacedOnHoldEvent

        then:
            onHold.bookId == availableBook.bookId
            onHold.byPatron == aPatron
            onHold.holdTill == oneHourLater
            onHold.holdPlacedAt == aBranch
            onHold.version == availableBook.version
    }
}
