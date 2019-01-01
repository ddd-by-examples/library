package io.pillopl.library.lending.domain.patron


import io.vavr.control.Either
import spock.lang.Specification

import static PatronBooksFixture.researcherPatronWithHolds
import static io.pillopl.library.lending.domain.book.BookFixture.circulatingBook
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldFailed
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold

class ResearcherPatronRequestingCirculatingBookTest extends Specification {

    def 'a researcher patron can hold any number of circulating books'() {
        when:
            Either<BookHoldFailed, BookPlacedOnHold> hold = researcherPatronWithHolds(holds).placeOnHold(circulatingBook())
        then:
            hold.isRight()
        where:
            holds << [0, 1, 2, 3, 4, 5, 100000]

    }
}
