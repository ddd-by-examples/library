package io.pillopl.library.lending.infrastructure.book

import io.pillopl.library.lending.domain.book.AvailableBook
import io.pillopl.library.lending.domain.book.Book
import io.pillopl.library.lending.domain.book.BookId
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronId
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.book.BookFixture.circulatingAvailableBookAt
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId

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
}
