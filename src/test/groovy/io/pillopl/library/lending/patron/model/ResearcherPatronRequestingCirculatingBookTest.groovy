package io.pillopl.library.lending.patron.model


import io.vavr.control.Either
import spock.lang.Specification

import static PatronFixture.researcherPatronWithHolds
import static io.pillopl.library.lending.book.model.BookFixture.circulatingBook
import static PatronEvent.BookHoldFailed
import static PatronEvent.BookPlacedOnHoldEvents

class ResearcherPatronRequestingCirculatingBookTest extends Specification {

    def 'a researcher patron can hold any number of circulating books'() {
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold = researcherPatronWithHolds(holds).placeOnHold(circulatingBook())
        then:
            hold.isRight()
        where:
            holds << [0, 1, 2, 3, 4, 5, 100000]

    }
}
