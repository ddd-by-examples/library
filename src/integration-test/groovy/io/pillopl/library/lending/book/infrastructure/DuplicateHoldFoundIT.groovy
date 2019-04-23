package io.pillopl.library.lending.book.infrastructure

import io.pillopl.library.common.events.publisher.DomainEventsTestConfig
import io.pillopl.library.lending.LendingTestContext
import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.BookFixture
import io.pillopl.library.lending.book.model.BookRepository
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.HoldDuration
import io.pillopl.library.lending.patron.model.Patron
import io.pillopl.library.lending.patron.model.Patrons
import io.pillopl.library.lending.patron.model.PatronId
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.sql.DataSource

import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHold.bookPlacedOnHoldNow
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronType.Regular

@SpringBootTest(classes = [LendingTestContext.class, DomainEventsTestConfig.class])
class DuplicateHoldFoundIT extends Specification {

    PatronId patron = anyPatronId()
    PatronId anotherPatron = anyPatronId()

    LibraryBranchId libraryBranchId = anyBranch()
    AvailableBook book = BookFixture.circulatingBook()

    @Autowired
    Patrons patronRepo

    @Autowired
    BookRepository bookRepository

    @Autowired
    DataSource datasource

    PollingConditions pollingConditions = new PollingConditions(timeout: 15)

    def 'should react to compensation event - duplicate hold on book found'() {
        given:
            bookRepository.save(book)
        and:
            patronRepo.publish(patronCreated(patron))
        and:
            patronRepo.publish(patronCreated(anotherPatron))
        when:
            patronRepo.publish(placedOnHold(book, patron))
        and:
            patronRepo.publish(placedOnHold(book, anotherPatron))
        then:
            patronShouldBeFoundInDatabaseWithZeroBookOnHold(anotherPatron)

    }

    BookPlacedOnHoldEvents placedOnHold(AvailableBook book, PatronId patronId) {
        return events(bookPlacedOnHoldNow(
                book.getBookId(),
                book.type(),
                book.libraryBranch,
                patronId,
                HoldDuration.closeEnded(5)))
    }

    PatronCreated patronCreated(PatronId patronId) {
        return PatronCreated.now(patronId, Regular)
    }

    void patronShouldBeFoundInDatabaseWithZeroBookOnHold(PatronId patronId) {
        pollingConditions.eventually {
            Patron patron = loadPersistedPatron(patronId)
            assert patron.numberOfHolds() == 0
        }
    }

    Patron loadPersistedPatron(PatronId patronId) {
        Option<Patron> loaded = patronRepo.findBy(patronId)
        Patron patron = loaded.getOrElseThrow({
            new IllegalStateException("should have been persisted")
        })
        return patron
    }
}
