package io.pillopl.library.lending.domain.patron


import io.vavr.control.Either
import spock.lang.Specification

import static PatronBooksEvent.BookHoldFailed
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldByPatron
import static PatronBooksFixture.regularPatron
import static io.pillopl.library.lending.domain.book.BookFixture.restrictedBook

class RegularPatronRequestingRestrictedBooksTest extends Specification {

    def 'a regular patron cannot place on hold restricted book'() {
        when:
            Either<BookHoldFailed, BookPlacedOnHoldByPatron> hold = regularPatron().placeOnHold(restrictedBook())
        then:
            hold.isLeft()
            BookHoldFailed e = hold.getLeft()
            e.reason.contains("Regular patrons cannot hold restricted books")
    }

}
