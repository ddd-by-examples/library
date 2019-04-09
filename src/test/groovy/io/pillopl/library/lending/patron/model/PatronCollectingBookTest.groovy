package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.BookOnHold
import io.vavr.control.Either
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

import static PatronEvent.BookCollectingFailed
import static PatronFixture.regularPatron
import static PatronFixture.regularPatronWith
import static io.pillopl.library.lending.book.model.BookFixture.bookOnHold
import static io.pillopl.library.lending.patron.model.CheckoutDuration.MAX_CHECKOUT_DURATION
import static io.pillopl.library.lending.patron.model.CheckoutDuration.maxDuration
import static io.pillopl.library.lending.patron.model.CheckoutDuration.forNoOfDays
import static PatronEvent.BookCollected
import static PatronFixture.onHold

class PatronCollectingBookTest extends Specification {

    def 'patron cannot collect book which is not placed on hold'() {
        when:
            Either<BookCollectingFailed, BookCollected> collect = regularPatron().collect(bookOnHold(), maxDuration())
        then:
            collect.isLeft()
            BookCollectingFailed e = collect.getLeft()
            e.reason.contains("book is not on hold by patron")
    }


    def 'patron can collect book which was placed on hold by him'() {
        given:
            Hold onHold = onHold()
        and:
            Patron patron = regularPatronWith(onHold)
        when:
            Either<BookCollectingFailed, BookCollected> collect = patron.collect(bookOnHold(onHold.bookId, onHold.libraryBranchId), maxDuration())
        then:
            collect.isRight()
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
            Either<BookCollectingFailed, BookCollected> collect = patron.collect(bookOnHold, forNoOfDays(checkoutTime, checkoutDays))
        then:
            collect.isRight()
            collect.get().with {
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
            patron.collect(bookOnHold(onHold.bookId, onHold.libraryBranchId), forNoOfDays(checkoutDays))
        then:
            thrown(IllegalArgumentException)
        where:
            checkoutDays << (-10 .. 0)
    }


}
