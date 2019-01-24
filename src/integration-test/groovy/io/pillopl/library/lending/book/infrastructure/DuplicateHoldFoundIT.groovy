package io.pillopl.library.lending.book.infrastructure

import io.pillopl.library.lending.LendingContext
import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.BookFixture
import io.pillopl.library.lending.book.model.BookRepository
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.*
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import javax.sql.DataSource

import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHold.bookPlacedOnHoldNow
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHoldEvents
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.PatronCreated
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronType.Regular

@ContextConfiguration(classes = [LendingContext.class])
@SpringBootTest
class DuplicateHoldFoundIT extends Specification {

    PatronId patron = anyPatronId()
    PatronId anotherPatron = anyPatronId()

    LibraryBranchId libraryBranchId = anyBranch()
    AvailableBook book = BookFixture.circulatingBook()

    @Autowired
    PatronBooksRepository patronBooksRepo

    @Autowired
    BookRepository bookRepository

    @Autowired
    DataSource datasource

    PollingConditions pollingConditions = new PollingConditions(timeout: 15)

    def 'should react to compensation event - duplicate hold on book found'() {
        given:
            bookRepository.save(book)
        and:
            patronBooksRepo.publish(patronCreated(patron))
        and:
            patronBooksRepo.publish(patronCreated(anotherPatron))
        when:
            patronBooksRepo.publish(placedOnHold(book, patron))
        and:
            patronBooksRepo.publish(placedOnHold(book, anotherPatron))
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
            PatronBooks patronBooks = loadPersistedPatron(patronId)
            assert patronBooks.numberOfHolds() == 0
        }
    }

    PatronBooks loadPersistedPatron(PatronId patronId) {
        Option<PatronBooks> loaded = patronBooksRepo.findBy(patronId)
        PatronBooks patronBooks = loaded.getOrElseThrow({
            new IllegalStateException("should have been persisted")
        })
        return patronBooks
    }
}
