package io.pillopl.library.lending.domain.patron


import io.vavr.control.Either
import spock.lang.Specification

import static PatronBooksEvent.BookCollectingFailed
import static PatronBooksFixture.regularPatron
import static PatronBooksFixture.regularPatronWith
import static io.pillopl.library.lending.domain.book.BookFixture.bookOnHold
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollectedByPatron
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.onHold

class PatronCollectingBookTest extends Specification {

    def 'patron cannot collect book which is not placed on hold'() {
        when:
            Either<BookCollectingFailed, BookCollectedByPatron> collect = regularPatron().collect(bookOnHold())
        then:
            collect.isLeft()
            BookCollectingFailed e = collect.getLeft()
            e.reason.contains("book is not on hold by patron")
    }


    def 'patron can collect book which was placed on hold by him'() {
        given:
            PatronHold onHold = onHold()
        and:
            PatronBooks patron = regularPatronWith(onHold)
        when:
            Either<BookCollectingFailed, BookCollectedByPatron> collect = patron.collect(bookOnHold(onHold.bookId, onHold.libraryBranchId))
        then:
            collect.isRight()
    }


}
