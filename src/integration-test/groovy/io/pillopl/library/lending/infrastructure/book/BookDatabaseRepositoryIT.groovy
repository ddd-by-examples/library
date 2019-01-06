package io.pillopl.library.lending.infrastructure.book

import io.pillopl.library.lending.domain.book.*
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.HoldDuration
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronInformation
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.book.BookFixture.circulatingAvailableBookAt
import static io.pillopl.library.lending.domain.book.BookType.Circulating
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold.now
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular

@ContextConfiguration(classes = BookDatabaseConfiguration.class)
@SpringBootTest
class BookDatabaseRepositoryIT extends Specification {

    BookId bookId = anyBookId()
    LibraryBranchId libraryBranchId = anyBranch()
    PatronId patronId = anyPatronId()

    @Autowired
    BookDatabaseRepository bookEntityRepository

    def 'persistence in real database should work'() {
        given:
            AvailableBook availableBook = circulatingAvailableBookAt(bookId, libraryBranchId)
        when:
            bookEntityRepository.save(availableBook)
        then:
            bookIsPersistedAs(AvailableBook.class)
        when:
            BookOnHold bookOnHold = availableBook.handle(placedOnHold())
        and:
            bookEntityRepository.save(bookOnHold)
        then:
            bookIsPersistedAs(BookOnHold.class)
        when:
            CollectedBook collectedBook = bookOnHold.handle(bookCollected())
        and:
            bookEntityRepository.save(collectedBook)
        then:
            bookIsPersistedAs(CollectedBook.class)

    }

    def 'should find available book'() {
        given:
            AvailableBook availableBook = circulatingAvailableBookAt(bookId, libraryBranchId)
        when:
            bookEntityRepository.save(availableBook)
        then:
            bookEntityRepository.findAvailableBookBy(bookId).isDefined()
        when:
            BookOnHold bookOnHold = availableBook.handle(placedOnHold())
        and:
            bookEntityRepository.save(bookOnHold)
        then:
            bookEntityRepository.findAvailableBookBy(bookId).isEmpty()
    }


    void bookIsPersistedAs(Class<?> clz) {
        Book book = loadPersistedBook(bookId)
        assert book.class == clz
    }

    BookDatabaseEntity bookEntity(BookDatabaseEntity.BookState state) {
        new BookDatabaseEntity(id: 1L,
                bookId: bookId.bookId,
                bookType: Circulating,
                bookState: state,
                availableAtBranch: libraryBranchId.libraryBranchId,
                onHoldAtBranch: libraryBranchId.libraryBranchId,
                onHoldByPatron: patronId.patronId,
                onHoldTill: Instant.now(),
                collectedAtBranch: libraryBranchId.libraryBranchId,
                collectedByPatron: patronId.patronId)
    }

    Book loadPersistedBook(BookId bookId) {
        Option<Book> loaded = bookEntityRepository.findBy(bookId)
        Book book = loaded.getOrElseThrow({
            new IllegalStateException("should have been persisted")
        })
        return book
    }

    BookPlacedOnHoldEvents placedOnHold() {
        return events(
                new PatronInformation(patronId, Regular), now(
                new BookInformation(bookId, Circulating),
                libraryBranchId,
                new PatronInformation(patronId, Regular),
                HoldDuration.forCloseEnded(5)))
    }

    PatronBooksEvent.BookCollected bookCollected() {
        return PatronBooksEvent.BookCollected.now(
                new BookInformation(bookId, Circulating),
                libraryBranchId,
                patronId)
    }

}
