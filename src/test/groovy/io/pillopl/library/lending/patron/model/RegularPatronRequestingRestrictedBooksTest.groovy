package io.pillopl.library.lending.patron.model


import io.vavr.control.Either
import spock.lang.Specification

import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookHoldFailed
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHoldEvents
import static PatronBooksFixture.regularPatron
import static io.pillopl.library.lending.book.model.BookFixture.restrictedBook

class RegularPatronRequestingRestrictedBooksTest extends Specification {

    def 'a regular patron cannot place on hold restricted book'() {
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold = regularPatron().placeOnHold(restrictedBook())
        then:
            hold.isLeft()
            BookHoldFailed e = hold.getLeft()
            e.reason.contains("Regular patrons cannot hold restricted books")
    }

}
