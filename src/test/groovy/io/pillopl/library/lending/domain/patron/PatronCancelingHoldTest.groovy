package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.book.BookFixture
import io.pillopl.library.lending.domain.book.BookOnHold
import io.vavr.control.Either
import spock.lang.Specification

import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.regularPatron
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.regularPatronWithHold
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldCanceled
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldCancelingFailed


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
