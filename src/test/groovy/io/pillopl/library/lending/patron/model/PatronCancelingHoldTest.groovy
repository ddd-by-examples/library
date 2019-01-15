package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.BookFixture
import io.pillopl.library.lending.book.model.BookOnHold
import io.vavr.control.Either
import spock.lang.Specification

import static PatronBooksFixture.regularPatron
import static PatronBooksFixture.regularPatronWithHold
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookHoldCanceled
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookHoldCancelingFailed


class PatronCancelingHoldTest extends Specification {

    def 'patron should be able to cancel his holds'() {
        given:
            BookOnHold forBook = BookFixture.bookOnHold()
        and:
            PatronBooks patron = regularPatronWithHold(forBook)
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
            PatronBooks patron = regularPatron()
        when:
            Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold = patron.cancelHold(forBook)
        then:
            cancelHold.isLeft()

    }

    def 'patron cannot cancel a hold which was done by someone else'() {
        given:
            BookOnHold forBook = BookFixture.bookOnHold()
        and:
            PatronBooks patron = regularPatron()
        and:
            PatronBooks differentPatron = regularPatronWithHold(forBook)
        when:
            Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold = patron.cancelHold(forBook)
        then:
            cancelHold.isLeft()

    }

}
