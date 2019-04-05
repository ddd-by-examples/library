package io.pillopl.library.lending.book.infrastructure

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.commons.aggregates.AggregateRootIsStale
import io.pillopl.library.commons.aggregates.Version
import io.pillopl.library.lending.LendingTestContext
import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.Book
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.HoldDuration
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import static io.pillopl.library.catalogue.BookType.Circulating
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.circulatingAvailableBookAt
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHold.bookPlacedOnHoldNow
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId

@SpringBootTest(classes = LendingTestContext.class)
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

    PatronEvent.BookPlacedOnHold placedOnHoldBy(PatronId patronId) {
        return events(bookPlacedOnHoldNow(
                bookId,
                Circulating,
                libraryBranchId,
                patronId,
                HoldDuration.closeEnded(5)))
                .bookPlacedOnHold
    }
}
