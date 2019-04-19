package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.BookOnHold
import io.vavr.control.Either
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

import static io.pillopl.library.lending.patron.model.PatronEvent.BookCheckingOutFailed
import static PatronFixture.regularPatron
import static PatronFixture.regularPatronWith
import static io.pillopl.library.lending.book.model.BookFixture.bookOnHold
import static io.pillopl.library.lending.patron.model.CheckoutDuration.MAX_CHECKOUT_DURATION
import static io.pillopl.library.lending.patron.model.CheckoutDuration.maxDuration
import static io.pillopl.library.lending.patron.model.CheckoutDuration.forNoOfDays
import static io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut
import static PatronFixture.onHold

class PatronCheckingOutBookTest extends Specification {

    def 'patron cannot check out book which is not placed on hold'() {
        when:
            Either<BookCheckingOutFailed, BookCheckedOut> checkOut = regularPatron().checkOut(bookOnHold(), maxDuration())
        then:
		checkOut.isLeft()
            BookCheckingOutFailed e = checkOut.getLeft()
            e.reason.contains("book is not on hold by patron")
    }


    def 'patron can check out book which was placed on hold by him'() {
        given:
            Hold onHold = onHold()
        and:
            Patron patron = regularPatronWith(onHold)
        when:
            Either<BookCheckingOutFailed, BookCheckedOut> checkOut = patron.checkOut(bookOnHold(onHold.bookId, onHold.libraryBranchId), maxDuration())
        then:
		checkOut.isRight()
    }

    def 'patron can checkout up to 60 days'() {
        given:
            Hold onHold = onHold()
        and:
            Instant checkoutTime = Instant.now()
        and:
            Patron patron = regularPatronWith(onHold)
        and:
            BookOnHold bookOnHold = bookOnHold(onHold.bookId, onHold.libraryBranchId)
        when:
            Either<BookCheckingOutFailed, BookCheckedOut> checkOut = patron.checkOut(bookOnHold, forNoOfDays(checkoutTime, checkoutDays))
        then:
		checkOut.isRight()
		checkOut.get().with {
                assert it.libraryBranchId == bookOnHold.holdPlacedAt.libraryBranchId
                assert it.bookId == bookOnHold.bookInformation.bookId.bookId
                assert it.till == checkoutTime.plus(Duration.ofDays(checkoutDays))

            }
        where:
            checkoutDays << (1 .. MAX_CHECKOUT_DURATION)
    }

    def 'patron cannot checkout for 0 or less'() {
        given:
            Hold onHold = onHold()
        and:
            Patron patron = regularPatronWith(onHold)
        when:
            patron.checkOut(bookOnHold(onHold.bookId, onHold.libraryBranchId), forNoOfDays(checkoutDays))
        then:
            thrown(IllegalArgumentException)
        where:
            checkoutDays << (-10 .. 0)
    }


}
