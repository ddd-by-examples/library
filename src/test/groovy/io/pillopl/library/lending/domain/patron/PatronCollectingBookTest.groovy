package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.book.Book
import io.pillopl.library.lending.domain.book.BookFixture
import io.vavr.control.Either
import spock.lang.Specification

import static PatronBooksFixture.regularPatron
import static PatronBooksFixture.regularPatronWith
import static PatronBooksEvent.BookCollected
import static PatronBooksEvent.BookCollectingFailed

class PatronCollectingBookTest extends Specification {

    def 'patron cannot collect book which is not placed on hold'() {
        when:
            Either<BookCollectingFailed, BookCollected> collect = regularPatron().collect(BookFixture.circulatingBook())
        then:
            collect.isLeft()
            BookCollectingFailed e = collect.getLeft()
            e.reason.contains("book is not on hold")
    }

    def 'patron cannot collect book which is placed on hold by another patron'() {
        given:
            PatronBooks patron = regularPatron()
            PatronBooks anotherPatron = regularPatron()
        and:
            Book book = BookFixture.circulatingBook()
        and:
            anotherPatron.placeOnHold(book)
        when:
            Either<BookCollectingFailed, BookCollected> collect = patron.collect(book)
        then:
            collect.isLeft()
            BookCollectingFailed e = collect.getLeft()
            e.reason.contains("book is not on hold by patron")
    }

    def 'patron can collect book which was placed on hold by him'() {
        given:
            BookOnHold onHold = BookFixture.onHold()
        and:
            PatronBooks patron = regularPatronWith(onHold)
        when:
            Either<BookCollectingFailed, BookCollected> collect = patron.collect(book(onHold))
        then:
            collect.isRight()
    }

    Book book(BookOnHold resourceOnHold) {
        return BookFixture.bookOnHold(resourceOnHold.bookId, resourceOnHold.libraryBranchId)
    }

}
