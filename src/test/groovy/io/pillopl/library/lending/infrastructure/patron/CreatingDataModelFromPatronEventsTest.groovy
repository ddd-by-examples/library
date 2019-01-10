package io.pillopl.library.lending.infrastructure.patron

import io.pillopl.library.lending.domain.book.BookInformation
import io.pillopl.library.lending.domain.book.BookType
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.*
import spock.lang.Specification

import java.time.Duration
import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.HoldDuration.closeEnded
import static io.pillopl.library.lending.domain.patron.HoldDuration.openEnded
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold.now
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular

class CreatingDataModelFromPatronEventsTest extends Specification {

    PatronId patronId = anyPatronId()
    PatronInformation.PatronType regular = Regular
    LibraryBranchId libraryBranchId = anyBranch()
    BookInformation bookInformation = new BookInformation(anyBookId(), BookType.Restricted)
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

    def 'should remove overdue checkout on bookReturned event'() {
        given:
            PatronBooksDatabaseEntity entity = createPatron()
        when:
            entity.handle(overdueCheckoutRegistered())
        then:
            entity.checkouts.size() == 1
        when:
            entity.handle(bookReturned())
        then:
            entity.checkouts.size() == 0


    }

    PatronBooksDatabaseEntity createPatron() {
        new PatronBooksDatabaseEntity(new PatronInformation(patronId, Regular))
    }

    PatronBooksEvent.BookCollected bookCollected() {
        return PatronBooksEvent.BookCollected.now(
                bookInformation,
                libraryBranchId,
                patronId,
                    CheckoutDuration.forNoOfDays(1))
    }

    PatronBooksEvent.BookReturned bookReturned() {
        return new PatronBooksEvent.BookReturned(
                Instant.now(),
                patronId.patronId,
                bookInformation.bookId.bookId,
                bookInformation.bookType,
                libraryBranchId.libraryBranchId)
    }

    PatronBooksEvent.BookHoldCanceled holdCanceled() {
        return PatronBooksEvent.BookHoldCanceled.now(
                bookInformation,
                libraryBranchId,
                new PatronInformation(patronId, Regular),
        )
    }

    PatronBooksEvent.BookPlacedOnHoldEvents placedOnHold(HoldDuration duration = closeEnded(5)) {
        return events(
                new PatronInformation(patronId, Regular), now(
                bookInformation,
                libraryBranchId,
                new PatronInformation(patronId, Regular),
                duration))
    }

    PatronBooksEvent.BookHoldExpired bookHoldExpired() {
        return PatronBooksEvent.BookHoldExpired.now(
                bookInformation.bookId,
                patronId,
                libraryBranchId
        )
    }

    PatronBooksEvent.OverdueCheckoutRegistered overdueCheckoutRegistered() {
        return PatronBooksEvent.OverdueCheckoutRegistered.now(patronId, bookInformation.bookId, libraryBranchId)
    }

}
