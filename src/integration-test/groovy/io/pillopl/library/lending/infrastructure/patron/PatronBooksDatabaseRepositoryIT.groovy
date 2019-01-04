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
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollected
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldCanceled
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular

@ContextConfiguration(classes = TestDatabaseConfig.class)
@SpringBootTest
class PatronBooksDatabaseRepositoryIT extends Specification {

    PatronId patronId = anyPatronId()
    LibraryBranchId libraryBranchId = anyBranch()
    BookInformation bookInformation = new BookInformation(anyBookId(), BookType.Restricted);

    @Autowired
    PatronBooksDatabaseRepository patronResourcesRepository

    def 'persistence in real database should work'() {
        when:
            patronResourcesRepository.reactTo(placedOnHold())
        then:
            patronShouldBeFoundInDatabaseWithOneBookOnHold(patronId)
        when:
            patronResourcesRepository.reactTo(patronCollected())
        then:
            patronShouldBeFoundInDatabaseWithZeroBooksOnHold(patronId)
        when:
            patronResourcesRepository.reactTo(placedOnHold())
        and:
            patronResourcesRepository.reactTo(holdCanceled())
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

    BookPlacedOnHold placedOnHold() {
        return BookPlacedOnHold.now(
                bookInformation,
                libraryBranchId,
                new PatronInformation(patronId, Regular),
                HoldDuration.forCloseEnded(5))
    }

    void patronShouldBeFoundInDatabaseWithOneBookOnHold(PatronId patronId) {
        PatronBooks patronBooks = loadPersistedPatron(patronId)
        assert patronBooks.numberOfHolds() == 1
    }

    void patronShouldBeFoundInDatabaseWithZeroBooksOnHold(PatronId patronId) {
        PatronBooks patronBooks = loadPersistedPatron(patronId)
        assert patronBooks.numberOfHolds() == 0
    }

    PatronBooks loadPersistedPatron(PatronId patronId) {
        Option<PatronBooks> loaded = patronResourcesRepository.findBy(patronId)
        PatronBooks patronBooks = loaded.getOrElseThrow({
            new IllegalStateException("should have been persisted")
        })
        return patronBooks
    }
}
