package io.pillopl.library.lending.book.application

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.catalogue.BookInstanceAddedToCatalogue
import io.pillopl.library.catalogue.BookType
import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.Book
import io.pillopl.library.lending.book.model.BookFixture
import io.pillopl.library.lending.book.model.BookRepository
import io.vavr.control.Option
import spock.lang.Specification

class CreatingAvailableBookForLendingTest extends Specification {

    static final BookId bookId = BookFixture.anyBookId()

    BookRepository bookRepository = new InMemoryBookRepo()

    CreateAvailableBookOnInstanceAddedEventHandler handler = new CreateAvailableBookOnInstanceAddedEventHandler(bookRepository)

    def 'should create new available book for lending when book instance was added to catalogue'() {
        when:
            handler.handle(new BookInstanceAddedToCatalogue("isbn", BookType.Restricted, bookId.getBookId()))
            Option<Book> book = bookRepository.findBy(bookId)
        then:
            book.isDefined()
            book.get() instanceof AvailableBook
            book.get().bookId() == bookId
            book.get().bookInformation.bookType == BookType.Restricted

    }
}


