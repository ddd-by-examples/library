package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.book.Book
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.vavr.control.Either
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.*
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldFailed
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.*
import static java.util.Collections.emptySet

class RegularPatronRequestingCirculatingBooksTest extends Specification {

    def 'a regular patron cannot place on hold book which is on hold'() {
        when:
            Either<BookHoldFailed, BookPlacedOnHold> hold = regularPatron().placeOnHold(bookOnHold())
        then:
            hold.isLeft()
            BookHoldFailed e = hold.getLeft()
            e.reason.contains("book is not available")

    }

    def 'a regular patron cannot place on hold more than 5 books'() {
        when:
            Either<BookHoldFailed, BookPlacedOnHold> hold = regularPatronWithHolds(holds).placeOnHold(circulatingBook())
        then:
            hold.isLeft()
            BookHoldFailed e = hold.getLeft()
            e.reason.contains("patron cannot hold more books")
        where:
            holds << [5, 6, 3000]

    }

    def 'a regular patron can place on hold book when he did not place on hold more than 4 books'() {
        given:
            Book book = circulatingBook()
        when:
            Either<BookHoldFailed, BookPlacedOnHold> hold = regularPatronWithHolds(holds).placeOnHold(book)
        then:
            hold.isRight()
        where:
            holds << [0, 1, 2, 3, 4]

    }

    def 'a regular patron cannot place on hold books anymore when he has at least two overdue checkouts'() {
        given:
            LibraryBranchId libraryBranchId = anyBranch()
        when:
            Either<BookHoldFailed, BookPlacedOnHold> hold =
                    regularPatronWithOverdueCheckouts(libraryBranchId, books).placeOnHold(circulatingResourceAt(libraryBranchId))
        then:
            hold.isLeft()
            BookHoldFailed e = hold.getLeft()
            e.reason.contains("cannot place on hold when there are overdue checkouts")
        where:
            books << [
                    [anyBookId(), anyBookId()] as Set,
                    [anyBookId(), anyBookId(), anyBookId()] as Set
            ]


    }

    def 'a regular patron can place on hold books when he does not have 2 overdues'() {
        given:
            Book book = circulatingBook()
        when:
            Either<BookHoldFailed, BookPlacedOnHold> hold = regularPatronWithOverdueCheckouts(anyBranch(), books).placeOnHold(book)
        then:
            hold.isRight()
        where:
            books <<  [[anyBookId()] as Set,
                       emptySet()]
    }


}
