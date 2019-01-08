package io.pillopl.library.lending.infrastructure.patron

import io.pillopl.library.lending.domain.book.BookInformation
import io.pillopl.library.lending.domain.book.BookType
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.HoldDuration
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronInformation
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold.now
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular

class CreatingDataModelFromPatronEventsTest extends Specification {

    PatronId patronId = anyPatronId()
    PatronInformation.PatronType regular = Regular
    LibraryBranchId libraryBranchId = anyBranch()
    BookInformation bookInformation = new BookInformation(anyBookId(), BookType.Restricted)

    def 'should react to patron holding events by adding and removing holds in data model'() {
        when:
            PatronBooksDatabaseEntity patronBooksRepo = createPatron()
        then:
            patronBooksRepo.booksOnHold.size() == 0
        when:
            patronBooksRepo.handle(placedOnHold())
        then:
            patronBooksRepo.booksOnHold.size() == 1
        when:
            patronBooksRepo.handle(patronCollected())
        then:
            patronBooksRepo.booksOnHold.size() == 0
        when:
            patronBooksRepo.handle(placedOnHold())
        then:
            patronBooksRepo.booksOnHold.size() == 1
        and:
            patronBooksRepo.handle(holdCanceled())
        then:
            patronBooksRepo.booksOnHold.size() == 0
        when:
            patronBooksRepo.handle(placedOnHold())
        then:
            patronBooksRepo.booksOnHold.size() == 1
        and:
            patronBooksRepo.handle(bookHoldExpired())
        then:
            patronBooksRepo.booksOnHold.size() == 0


    }

    PatronBooksDatabaseEntity createPatron() {
        new PatronBooksDatabaseEntity(new PatronInformation(patronId, Regular))
    }

    PatronBooksEvent.BookCollected patronCollected() {
        return PatronBooksEvent.BookCollected.now(
                bookInformation,
                libraryBranchId,
                patronId)
    }

    PatronBooksEvent.BookHoldCanceled holdCanceled() {
        return PatronBooksEvent.BookHoldCanceled.now(
                bookInformation,
                libraryBranchId,
                new PatronInformation(patronId, Regular),
        )
    }

    PatronBooksEvent.BookPlacedOnHoldEvents placedOnHold() {
        return events(
                new PatronInformation(patronId, Regular), now(
                bookInformation,
                libraryBranchId,
                new PatronInformation(patronId, Regular),
                HoldDuration.forCloseEnded(5)))
    }

    PatronBooksEvent.BookHoldExpired bookHoldExpired() {
        return PatronBooksEvent.BookHoldExpired.now(
                bookInformation.bookId,
                patronId,
                libraryBranchId
        )
    }

}
