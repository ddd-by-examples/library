package io.pillopl.library.lending.infrastructure.patron

import io.pillopl.library.lending.domain.book.BookInformation
import io.pillopl.library.lending.domain.book.BookType
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.*
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.*
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold.now
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.regularPatron
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular

@ContextConfiguration(classes = PatronDatabaseConfiguration.class)
@SpringBootTest
class PatronBooksDatabaseRepositoryIT extends Specification {

    PatronId patronId = anyPatronId()
    PatronInformation.PatronType regular = Regular
    LibraryBranchId libraryBranchId = anyBranch()
    BookInformation bookInformation = new BookInformation(anyBookId(), BookType.Restricted);

    @Autowired
    PatronBooksRepository patronResourcesRepository

    def 'persistence in real database should work'() {
        when:
            patronResourcesRepository.handle(patronCreated())
        then:
            patronShouldBeFoundInDatabaseWithZeroBooksOnHold(patronId)
        when:
            patronResourcesRepository.handle(placedOnHold())
        then:
            patronShouldBeFoundInDatabaseWithOneBookOnHold(patronId)
        when:
            patronResourcesRepository.handle(patronCollected())
        then:
            patronShouldBeFoundInDatabaseWithZeroBooksOnHold(patronId)
        when:
            patronResourcesRepository.handle(placedOnHold())
        then:
            patronShouldBeFoundInDatabaseWithOneBookOnHold(patronId)
        and:
            patronResourcesRepository.handle(holdCanceled())
        then:
            patronShouldBeFoundInDatabaseWithZeroBooksOnHold(patronId)

    }

    BookCollected patronCollected() {
        return BookCollected.now(
                bookInformation,
                libraryBranchId,
                patronId)
    }

    BookHoldCanceled holdCanceled() {
        return BookHoldCanceled.now(
                bookInformation.bookId,
                libraryBranchId,
                new PatronInformation(patronId, Regular),
        )
    }

    BookPlacedOnHoldEvents placedOnHold() {
        return events(
                new PatronInformation(patronId, Regular), now(
                bookInformation,
                libraryBranchId,
                new PatronInformation(patronId, Regular),
                HoldDuration.forCloseEnded(5)))
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
        Option<PatronBooks> loaded = patronResourcesRepository.findBy(patronId)
        PatronBooks patronBooks = loaded.getOrElseThrow({
            new IllegalStateException("should have been persisted")
        })
        return patronBooks
    }
}
