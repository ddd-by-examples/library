package io.pillopl.library.lending.patron.infrastructure

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.catalogue.BookType
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.*
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.catalogue.BookType.Restricted
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.CheckoutDuration.forNoOfDays
import static io.pillopl.library.lending.patron.model.HoldDuration.closeEnded
import static io.pillopl.library.lending.patron.model.HoldDuration.openEnded
import static io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled.holdCanceledNow
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHold.bookPlacedOnHoldNow
import static io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronType.Regular

class CreatingDataModelFromPatronEventsTest extends Specification {

    PatronId patronId = anyPatronId()
    PatronType regular = Regular
    LibraryBranchId libraryBranchId = anyBranch()
    BookType type = Restricted
    BookId bookId = anyBookId()
    Instant holdFrom = Instant.now()

    def 'should add hold on placedOnHold event with close ended duration'() {
        given:
            PatronDatabaseEntity entity = createPatron()
        when:
            entity.handle(placedOnHold(closeEnded(holdFrom, NumberOfDays.of(1))))
        then:
            entity.booksOnHold.size() == 1
            entity.booksOnHold.iterator().next().till == holdFrom.plus(Duration.ofDays(1))

    }

    def 'should add hold on placedOnHold event with open ended duration '() {
        given:
            PatronDatabaseEntity entity = createPatron()
        when:
            entity.handle(placedOnHold(openEnded()))
        then:
            entity.booksOnHold.size() == 1
            entity.booksOnHold.iterator().next().till == null

    }

    def 'should remove hold on patronCheckedOut event'() {
        given:
            PatronDatabaseEntity entity = createPatron()
        when:
            entity.handle(placedOnHold())
        then:
            entity.booksOnHold.size() == 1
        when:
            entity.handle(bookCheckedOut())
        then:
            entity.booksOnHold.size() == 0

    }

    def 'should remove hold on holdCancelled event'() {
        given:
            PatronDatabaseEntity entity = createPatron()
        when:
            entity.handle(placedOnHold())
        then:
            entity.booksOnHold.size() == 1
        when:
            entity.handle(holdCanceled())
        then:
            entity.booksOnHold.size() == 0
    }

    def 'should remove hold on holdExpired event'() {
        given:
            PatronDatabaseEntity entity = createPatron()
        when:
            entity.handle(placedOnHold())
        then:
            entity.booksOnHold.size() == 1
        when:
            entity.handle(bookHoldExpired())
        then:
            entity.booksOnHold.size() == 0

    }

    def 'should add overdue checkout on overdueCheckoutRegistered'() {
        given:
            PatronDatabaseEntity entity = createPatron()
        when:
            entity.handle(overdueCheckoutRegistered())
        then:
            entity.checkouts.size() == 1
    }

    def 'should remove overdue checkout on bookReturned event'() {
        given:
            PatronDatabaseEntity entity = createPatron()
        when:
            entity.handle(overdueCheckoutRegistered())
        then:
            entity.checkouts.size() == 1
        when:
            entity.handle(bookReturned())
        then:
            entity.checkouts.size() == 0


    }

    PatronDatabaseEntity createPatron() {
        return new PatronDatabaseEntity(patronId, Regular)
    }

	PatronEvent.BookCheckedOut bookCheckedOut() {
        return PatronEvent.BookCheckedOut.bookCheckedOutNow(
                bookId,
                type,
                libraryBranchId,
                patronId,
                forNoOfDays(1))
    }

    PatronEvent.BookReturned bookReturned() {
        return new PatronEvent.BookReturned(
                Instant.now(),
                patronId.patronId,
                bookId.bookId,
                type,
                libraryBranchId.libraryBranchId)
    }

    PatronEvent.BookHoldCanceled holdCanceled() {
        return holdCanceledNow(
                bookId,
                libraryBranchId,
                patronId)
    }

    PatronEvent.BookPlacedOnHoldEvents placedOnHold(HoldDuration duration = closeEnded(5)) {
        return events(bookPlacedOnHoldNow(
                bookId,
                type,
                libraryBranchId,
                patronId,
                duration))
    }

    PatronEvent.BookHoldExpired bookHoldExpired() {
        return PatronEvent.BookHoldExpired.now(
                bookId,
                patronId,
                libraryBranchId
        )
    }

    PatronEvent.OverdueCheckoutRegistered overdueCheckoutRegistered() {
        return PatronEvent.OverdueCheckoutRegistered.now(patronId, bookId, libraryBranchId)
    }

}
