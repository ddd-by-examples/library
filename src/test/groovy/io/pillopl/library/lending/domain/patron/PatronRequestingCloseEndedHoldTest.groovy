package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.book.AvailableBook
import io.vavr.control.Either
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.circulatingAvailableBook
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldFailed
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.regularPatronWithPolicy
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.researcherPatronWithPolicy
import static io.pillopl.library.lending.domain.patron.PlacingOnHoldPolicy.onlyResearcherPatronsCanPlaceOpenEndedHolds

class PatronRequestingCloseEndedHoldTest extends Specification {

    Instant from = Instant.MIN

    def 'any patron can request close ended hold'() {
        given:
            AvailableBook aBook = circulatingAvailableBook()
        when:
            Either<BookHoldFailed, BookPlacedOnHold> hold =
                    patron
                    .placeOnHold(aBook, HoldDuration.forCloseEnded(from, 3))
        then:
            hold.isRight()
            hold.get().with {
                assert it.libraryBranchId == aBook.libraryBranch.libraryBranchId
                assert it.bookId == aBook.bookInformation.bookId.bookId
                assert it.holdFrom == from
                assert it.holdTill == from.plus(Duration.ofDays(3))
            }
        where:
            patron << [regularPatronWithPolicy(onlyResearcherPatronsCanPlaceOpenEndedHolds),
                       researcherPatronWithPolicy(onlyResearcherPatronsCanPlaceOpenEndedHolds)]

    }


}
