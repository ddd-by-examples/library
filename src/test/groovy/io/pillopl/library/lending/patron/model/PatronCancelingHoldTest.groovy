package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.BookFixture
import io.pillopl.library.lending.book.model.BookOnHold
import io.vavr.control.Either
import spock.lang.Specification

import static PatronFixture.regularPatron
import static PatronFixture.regularPatronWithHold
import static PatronEvent.BookHoldCanceled
import static PatronEvent.BookHoldCancelingFailed


class PatronCancelingHoldTest extends Specification {

    def 'patron should be able to cancel his holds'() {
        given:
            BookOnHold forBook = BookFixture.bookOnHold()
        and:
            Patron patron = regularPatronWithHold(forBook)
        when:
            Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold = patron.cancelHold(forBook)
        then:
            cancelHold.isRight()
            cancelHold.get().with {
                assert it.libraryBranchId == forBook.getHoldPlacedAt().libraryBranchId
                assert it.bookId == forBook.bookInformation.bookId.bookId
            }

    }

    def 'patron cannot cancel a hold which does not exist'() {
        given:
            BookOnHold forBook = BookFixture.bookOnHold()
        and:
            Patron patron = regularPatron()
        when:
            Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold = patron.cancelHold(forBook)
        then:
            cancelHold.isLeft()

    }

    def 'patron cannot cancel a hold which was done by someone else'() {
        given:
            BookOnHold forBook = BookFixture.bookOnHold()
        and:
            Patron patron = regularPatron()
        and:
            Patron differentPatron = regularPatronWithHold(forBook)
        when:
            Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold = patron.cancelHold(forBook)
        then:
            cancelHold.isLeft()

    }

}
