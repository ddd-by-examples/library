package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.book.Book
import io.vavr.control.Either
import spock.lang.Specification

import java.time.Instant

import static PatronBooksFixture.regularPatron
import static io.pillopl.library.lending.domain.book.BookFixture.circulatingBook
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.*

class PatronReturningBookTest extends Specification {

    def 'patron can return book which is marked as placed on hold in the system'() {
        given:
            PatronBooks patron = regularPatron()
            Book book = circulatingBook()
        and:
            patron.placeOnHold(book)
        when:
            book.handle(bookReturned())
        then:
            book.isAvailable()
    }

    def 'patron can return book which is marked as collected in the syastem'() {
        given:
            PatronBooks patron = regularPatron()
            Book book = circulatingBook()
        and:
            patron.placeOnHold(book)
        and:
            patron.collect(book)
        when:
            book.handle(bookReturned())
        then:
            book.isAvailable()
    }

    def 'a patron can place on hold book which was just returned'() {
        given:
            PatronBooks patron = regularPatron()
            Book book = circulatingBook()
        and:
            patron.placeOnHold(book)
        and:
            patron.collect(book)
        when:
            book.handle(bookReturned())
        and:
            Either<BookHoldFailed, BookPlacedOnHold> hold = patron.placeOnHold(book)
        then:
            hold.isRight()
    }

    BookReturned bookReturned() {
        new BookReturned(Instant.now(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
    }

}
