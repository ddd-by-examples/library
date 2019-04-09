package io.pillopl.library.lending.book.application

import io.pillopl.library.commons.events.DomainEvent
import io.pillopl.library.commons.events.DomainEvents
import io.pillopl.library.lending.book.model.BookDuplicateHoldFound
import io.pillopl.library.lending.book.model.BookFixture
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.book.model.BookRepository
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.vavr.control.Option
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId

class DuplicateHoldFoundTest extends Specification {

    BookOnHold bookOnHold = BookFixture.bookOnHold()
    BookRepository bookRepository = Stub()
    DomainEvents domainEvents = Mock()
    PatronEventsHandler patronEventsHandler = new PatronEventsHandler(bookRepository, domainEvents)

    PatronId patronId = anyPatronId()
    LibraryBranchId libraryBranchId = anyBranch()


    def 'should raise duplicate hold found event when someone placed on hold book already on hold'() {
        given:
            bookIsAlreadyOnHold()
        when:
            patronEventsHandler.handle(placedOnHoldBy(patronId))
        then:
            1 * domainEvents.publish({
                it.firstPatronId == bookOnHold.byPatron.patronId &&
                it.secondPatronId == patronId.patronId
            } as BookDuplicateHoldFound)
    }


    def 'should not raise anything if book is on hold by the same patron'() {
        given:
            bookIsAlreadyOnHold()
        when:
            patronEventsHandler.handle(placedOnHoldBy(bookOnHold.byPatron))
        then:
            0 * domainEvents.publish(_ as DomainEvent)
    }

    PatronEvent.BookPlacedOnHold placedOnHoldBy(PatronId patronId) {
        return new PatronEvent.BookPlacedOnHold(Instant.now(), patronId.patronId, bookOnHold.bookId.bookId, bookOnHold.bookInformation.bookType, libraryBranchId.libraryBranchId, Instant.now(), Instant.now())
    }

    void bookIsAlreadyOnHold() {
        bookRepository.findBy(bookOnHold.bookId) >> Option.of(bookOnHold)
    }
}
