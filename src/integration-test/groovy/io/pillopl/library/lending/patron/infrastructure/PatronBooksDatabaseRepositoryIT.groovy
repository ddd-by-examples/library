package io.pillopl.library.lending.patron.infrastructure

import io.pillopl.library.lending.book.model.BookInformation
import io.pillopl.library.lending.book.model.BookType
import io.pillopl.library.lending.library.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.HoldDuration
import io.pillopl.library.lending.patron.model.PatronBooks
import io.pillopl.library.lending.patron.model.PatronBooksRepository
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.PatronInformation
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHold.bookPlacedOnHoldNow
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHoldEvents
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.PatronCreated
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.regularPatron
import static io.pillopl.library.lending.patron.model.PatronInformation.PatronType.Regular

@ContextConfiguration(classes = PatronDatabaseConfiguration.class)
@SpringBootTest
class PatronBooksDatabaseRepositoryIT extends Specification {

    PatronId patronId = anyPatronId()
    PatronInformation.PatronType regular = Regular
    LibraryBranchId libraryBranchId = anyBranch()
    BookInformation bookInformation = new BookInformation(anyBookId(), BookType.Restricted)

    @Autowired
    PatronBooksRepository patronBooksRepo

    def 'persistence in real database should work'() {
        when:
            patronBooksRepo.publish(patronCreated())
        then:
            patronShouldBeFoundInDatabaseWithZeroBooksOnHold(patronId)
        when:
            patronBooksRepo.publish(placedOnHold())
        then:
            patronShouldBeFoundInDatabaseWithOneBookOnHold(patronId)
    }

    BookPlacedOnHoldEvents placedOnHold() {
        return events(bookPlacedOnHoldNow(
                bookInformation,
                libraryBranchId,
                new PatronInformation(patronId, Regular),
                HoldDuration.closeEnded(5)))
    }

    PatronCreated patronCreated() {
        return PatronCreated.now(new PatronInformation(patronId, Regular))
    }

    void patronShouldBeFoundInDatabaseWithOneBookOnHold(PatronId patronId) {
        PatronBooks patronBooks = loadPersistedPatron(patronId)
        assert patronBooks.numberOfHolds() == 1
        assertPatronInformation(patronBooks, patronId)
    }

    void patronShouldBeFoundInDatabaseWithZeroBooksOnHold(PatronId patronId) {
        PatronBooks patronBooks = loadPersistedPatron(patronId)
        assert patronBooks.numberOfHolds() == 0
        assertPatronInformation(patronBooks, patronId)

    }

    void assertPatronInformation(PatronBooks patronBooks, PatronId patronId) {
        assert patronBooks.equals(regularPatron(patronId))
    }

    PatronBooks loadPersistedPatron(PatronId patronId) {
        Option<PatronBooks> loaded = patronBooksRepo.findBy(patronId)
        PatronBooks patronBooks = loaded.getOrElseThrow({
            new IllegalStateException("should have been persisted")
        })
        return patronBooks
    }
}
