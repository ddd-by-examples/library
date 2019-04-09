package io.pillopl.library.lending.patron.model

import io.pillopl.library.lending.book.model.AvailableBook
import io.vavr.control.Either
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.circulatingAvailableBook
import static PatronEvent.BookHoldFailed
import static PatronEvent.BookPlacedOnHold
import static PatronEvent.BookPlacedOnHoldEvents
import static PatronFixture.*
import static io.pillopl.library.lending.patron.model.PlacingOnHoldPolicy.onlyResearcherPatronsCanPlaceOpenEndedHolds

class PatronRequestingOpenEndedHoldTest extends Specification {

    Instant from = Instant.MIN

    def 'researcher patron can request close ended hold'() {
        given:
            AvailableBook aBook = circulatingAvailableBook()
        and:
            PatronId patronId = anyPatronId()
        and:
            Patron researcherPatron = researcherPatronWithPolicy(patronId, onlyResearcherPatronsCanPlaceOpenEndedHolds)
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold = researcherPatron.placeOnHold(aBook, HoldDuration.openEnded(from))
        then:
            hold.isRight()
            hold.get().with {
                BookPlacedOnHold bookPlacedOnHold = it.bookPlacedOnHold
                assert bookPlacedOnHold.libraryBranchId == aBook.libraryBranch.libraryBranchId
                assert bookPlacedOnHold.patronId == patronId.patronId
                assert bookPlacedOnHold.bookId == aBook.bookInformation.bookId.bookId
                assert bookPlacedOnHold.holdFrom == from
                assert bookPlacedOnHold.holdTill == null
            }

    }

    def 'regular patron cannot request open ended hold'() {
        given:
            AvailableBook aBook = circulatingAvailableBook()
        and:
            PatronId patronId = anyPatronId()
        and:
            Patron regularPatron = regularPatronWithPolicy(patronId, onlyResearcherPatronsCanPlaceOpenEndedHolds)
        when:
            Either<BookHoldFailed, BookPlacedOnHoldEvents> hold = regularPatron.placeOnHold(aBook, HoldDuration.openEnded(from))
        then:
            hold.isLeft()
            hold.getLeft().with {
                assert it.reason.contains("regular patron cannot place open ended holds")
                assert it.libraryBranchId == aBook.libraryBranch.libraryBranchId
                assert it.patronId == patronId.patronId
                assert it.bookId == aBook.bookInformation.bookId.bookId
            }

    }


}
