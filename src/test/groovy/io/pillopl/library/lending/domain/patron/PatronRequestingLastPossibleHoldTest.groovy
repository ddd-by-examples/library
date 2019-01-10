package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.book.AvailableBook
import io.vavr.control.Either
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.circulatingBook
import static io.pillopl.library.lending.domain.patron.HoldDuration.closeEnded
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.*
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.regularPatronWithHolds

class PatronRequestingLastPossibleHoldTest extends Specification {


    def 'should announce that a regular patron places his last possible hold (4th)'() {
        given:
            AvailableBook book = circulatingBook()
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold = regularPatronWithHolds(4).placeOnHold(book, closeEnded(3))
        then:
            hold.isRight()
            hold.get().with {
                assert it.maximumNumberOhHoldsReached.isDefined()
                MaximumNumberOhHoldsReached maximumNumberOhHoldsReached = it.maximumNumberOhHoldsReached.get()
                assert maximumNumberOhHoldsReached.numberOfHolds == 5
            }

    }


}
