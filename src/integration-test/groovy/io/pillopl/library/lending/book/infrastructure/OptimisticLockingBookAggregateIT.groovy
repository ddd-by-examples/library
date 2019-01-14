package io.pillopl.library.lending.book.infrastructure

import io.pillopl.library.commons.aggregates.AggregateRootIsStale
import io.pillopl.library.commons.aggregates.Version
import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.Book
import io.pillopl.library.lending.book.model.BookId
import io.pillopl.library.lending.book.model.BookInformation
import io.pillopl.library.lending.library.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.HoldDuration
import io.pillopl.library.lending.patron.model.PatronBooksEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.PatronInformation
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.circulatingAvailableBookAt
import static io.pillopl.library.lending.book.model.BookType.Circulating
import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHold.bookPlacedOnHoldNow
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronInformation.PatronType.Regular

@ContextConfiguration(classes = BookConfiguration.class)
@SpringBootTest
class OptimisticLockingBookAggregateIT extends Specification {

    BookId bookId = anyBookId()
    LibraryBranchId libraryBranchId = anyBranch()
    PatronId patronId = anyPatronId()

    @Autowired
    BookDatabaseRepository bookEntityRepository

    def 'persistence in real database should work'() {
        given:
            AvailableBook availableBook = circulatingAvailableBookAt(bookId, libraryBranchId)
        and:
            bookEntityRepository.save(availableBook)
        and:
            Book loaded = loadPersistedBook(bookId)
        and:
            someoneModifiedBookInTheMeantime(availableBook)
        when:
            bookEntityRepository.save(loaded)
        then:
            thrown(AggregateRootIsStale)
            loadPersistedBook(bookId).version == new Version(1)
    }

    void someoneModifiedBookInTheMeantime(AvailableBook availableBook) {
        bookEntityRepository.save(availableBook.handle(placedOnHoldBy(anyPatronId())))
    }

    void bookIsPersistedAs(Class<?> clz) {
        Book book = loadPersistedBook(bookId)
        assert book.class == clz
    }

    Book loadPersistedBook(BookId bookId) {
        Option<Book> loaded = bookEntityRepository.findBy(bookId)
        Book book = loaded.getOrElseThrow({
            new IllegalStateException("should have been persisted")
        })
        return book
    }

    PatronBooksEvent.BookPlacedOnHold placedOnHoldBy(PatronId patronId) {
        return events(bookPlacedOnHoldNow(
                new BookInformation(bookId, Circulating),
                libraryBranchId,
                new PatronInformation(patronId, Regular),
                HoldDuration.closeEnded(5)))
                .bookPlacedOnHold
    }
}
