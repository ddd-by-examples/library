package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.book.AvailableBook
import io.vavr.control.Either
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.circulatingAvailableBook
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldFailed
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.regularPatronWithPolicy
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.researcherPatronWithPolicy

import static io.pillopl.library.lending.domain.patron.PlacingOnHoldPolicy.onlyResearcherPatronsCanPlaceOpenEndedHolds

class PatronRequestingCloseEndedHoldTest extends Specification {

    Instant from = Instant.MIN

    def 'any patron can request close ended hold'() {
        given:
            AvailableBook aBook = circulatingAvailableBook()
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold = patron.placeOnHold(aBook, HoldDuration.forCloseEnded(from, NumberOfDays.of(3)))
        then:
            hold.isRight()
            hold.get().with {
                BookPlacedOnHold bookPlacedOnHold = it.bookPlacedOnHold
                assert bookPlacedOnHold.libraryBranchId == aBook.libraryBranch.libraryBranchId
                assert bookPlacedOnHold.bookId == aBook.bookInformation.bookId.bookId
                assert bookPlacedOnHold.holdFrom == from
                assert bookPlacedOnHold.holdTill == from.plus(Duration.ofDays(3))
                assert it.maximumNumberOhHoldsReached.isEmpty()
            }
        where:
            patron << [regularPatronWithPolicy(onlyResearcherPatronsCanPlaceOpenEndedHolds),
                       researcherPatronWithPolicy(onlyResearcherPatronsCanPlaceOpenEndedHolds)]

    }


}
