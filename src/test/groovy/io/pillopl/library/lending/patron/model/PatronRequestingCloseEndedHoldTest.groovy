package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.AvailableBook
import io.vavr.control.Either
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.circulatingAvailableBook
import static PatronEvent.BookHoldFailed
import static PatronEvent.BookPlacedOnHold
import static PatronEvent.BookPlacedOnHoldEvents
import static PatronFixture.regularPatron
import static PatronFixture.regularPatronWithPolicy
import static PatronFixture.researcherPatronWithPolicy

import static io.pillopl.library.lending.patron.model.PlacingOnHoldPolicy.onlyResearcherPatronsCanPlaceOpenEndedHolds

class PatronRequestingCloseEndedHoldTest extends Specification {

    Instant from = Instant.MIN

    def 'any patron can request close ended hold'() {
        given:
            AvailableBook aBook = circulatingAvailableBook()
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold = patron.placeOnHold(aBook, HoldDuration.closeEnded(from, NumberOfDays.of(3)))
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

    def 'patron cannot hold a book for 0 or negative amount of days'() {
        given:
            AvailableBook aBook = circulatingAvailableBook()
        and:
            Patron patron = regularPatron()
        when:
            patron.placeOnHold(aBook, HoldDuration.closeEnded(from, NumberOfDays.of(days)))
        then:
           thrown(IllegalArgumentException)

        where:
            days << (-10 .. 0)

    }


}
