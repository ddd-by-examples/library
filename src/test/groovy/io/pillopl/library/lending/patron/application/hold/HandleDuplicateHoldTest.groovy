package io.pillopl.library.lending.patron.application.hold

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.lending.book.model.BookDuplicateHoldFound
import io.pillopl.library.lending.patron.model.PatronId
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static java.time.Instant.now

class HandleDuplicateHoldTest extends Specification {

    CancelingHold cancelingHold = Mock()

    def "should start cancelling hold if book was already hold by other patron"() {
        given:
            Clock clock = Clock.fixed(Instant.parse('2020-02-27T12:21:00Z'), ZoneOffset.UTC)
        and:
            HandleDuplicateHold duplicateHold = new HandleDuplicateHold(cancelingHold, clock)
        and:
            BookDuplicateHoldFound bookDuplicateHoldFound = duplicateHoldFoundBy()
        and:
            CancelHoldCommand cancelHoldCommand = cancelHoldCommandFrom(bookDuplicateHoldFound, clock)
        when:
            duplicateHold.handle(bookDuplicateHoldFound)
        then:
            1 * cancelingHold.cancelHold(cancelHoldCommand)
    }

    BookDuplicateHoldFound duplicateHoldFoundBy() {
        return new BookDuplicateHoldFound(
                now(),
                anyPatronId().patronId,
                anyPatronId().patronId,
                anyBranch().libraryBranchId,
                anyBookId().bookId
        )
    }

    CancelHoldCommand cancelHoldCommandFrom(BookDuplicateHoldFound event, Clock clock) {
        return new CancelHoldCommand(
                clock.instant(),
                new PatronId(event.getSecondPatronId()),
                new BookId(event.getBookId())
        )
    }

}
