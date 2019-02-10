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
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookHoldCanceled.holdCanceledNow
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHold.bookPlacedOnHoldNow
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId
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
            PatronBooksDatabaseEntity entity = createPatron()
        when:
            entity.handle(placedOnHold(closeEnded(holdFrom, NumberOfDays.of(1))))
        then:
            entity.booksOnHold.size() == 1
            entity.booksOnHold.iterator().next().till == holdFrom.plus(Duration.ofDays(1))

    }

    def 'should add hold on placedOnHold event with open ended duration '() {
        given:
            PatronBooksDatabaseEntity entity = createPatron()
        when:
            entity.handle(placedOnHold(openEnded()))
        then:
            entity.booksOnHold.size() == 1
            entity.booksOnHold.iterator().next().till == null

    }

    def 'should remove hold on patronCollected event'() {
        given:
            PatronBooksDatabaseEntity entity = createPatron()
        when:
            entity.handle(placedOnHold())
        then:
            entity.booksOnHold.size() == 1
        when:
            entity.handle(bookCollected())
        then:
            entity.booksOnHold.size() == 0

    }

    def 'should remove hold on holdCancelled event'() {
        given:
            PatronBooksDatabaseEntity entity = createPatron()
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
            PatronBooksDatabaseEntity entity = createPatron()
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
            PatronBooksDatabaseEntity entity = createPatron()
        when:
            entity.handle(overdueCheckoutRegistered())
        then:
            entity.checkouts.size() == 1
    }

    def 'should remove overdue checkout on bookCheckedOut event'() {
        given:
            PatronBooksDatabaseEntity entity = createPatron()
        when:
            entity.handle(overdueCheckoutRegistered())
        then:
            entity.checkouts.size() == 1
        when:
            entity.handle(bookCheckedOut())
        then:
            entity.checkouts.size() == 0


    }

    PatronBooksDatabaseEntity createPatron() {
        return new PatronBooksDatabaseEntity(patronId, Regular)
    }

    PatronBooksEvent.BookCollected bookCollected() {
        return PatronBooksEvent.BookCollected.bookCollectedNow(
                bookId,
                type,
                libraryBranchId,
                patronId,
                forNoOfDays(1))
    }

    PatronBooksEvent.BookCheckedOut bookCheckedOut() {
        return new PatronBooksEvent.BookCheckedOut(
                Instant.now(),
                patronId.patronId,
                bookId.bookId,
                type,
                libraryBranchId.libraryBranchId)
    }

    PatronBooksEvent.BookHoldCanceled holdCanceled() {
        return holdCanceledNow(
                bookId,
                libraryBranchId,
                patronId)
    }

    PatronBooksEvent.BookPlacedOnHoldEvents placedOnHold(HoldDuration duration = closeEnded(5)) {
        return events(bookPlacedOnHoldNow(
                bookId,
                type,
                libraryBranchId,
                patronId,
                duration))
    }

    PatronBooksEvent.BookHoldExpired bookHoldExpired() {
        return PatronBooksEvent.BookHoldExpired.now(
                bookId,
                patronId,
                libraryBranchId
        )
    }

    PatronBooksEvent.OverdueCheckoutRegistered overdueCheckoutRegistered() {
        return PatronBooksEvent.OverdueCheckoutRegistered.now(patronId, bookId, libraryBranchId)
    }

}
