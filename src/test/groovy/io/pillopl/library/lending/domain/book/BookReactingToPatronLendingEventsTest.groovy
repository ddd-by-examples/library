package io.pillopl.library.lending.domain.book

import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.*
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookReturnedByPatron

class BookReactingToPatronLendingEventsTest extends Specification {

    def 'should return book which is marked as placed on hold in the system'() {
        given:
            BookOnHold onHold = bookOnHold()
        when:
            AvailableBook available = onHold.handle(bookReturned(onHold))
        then:
            available.bookId == onHold.bookId
    }

    def 'should place on hold book which is marked as available in the system'() {
        given:
            AvailableBook available = circulatingAvailableBook()
        when:
            BookOnHold onHold = available.handle(bookPlacedOnHold(available))
        then:
            onHold.bookId == available.bookId
    }

    def 'should return book which is marked as collected in the system'() {
        given:
            CollectedBook collected = collectedBook()
        when:
            AvailableBook available = collected.handle(bookReturned(collected))
        then:
            available.bookId == collected.bookId
    }

    BookReturnedByPatron bookReturned(CollectedBook bookCollected) {
        return new BookReturnedByPatron(Instant.now(), UUID.randomUUID(), bookCollected.getBookId().bookId, bookCollected.bookInformation.bookType, UUID.randomUUID())
    }

    BookReturnedByPatron bookReturned(BookOnHold bookOnHold) {
        return new BookReturnedByPatron(Instant.now(), UUID.randomUUID(), bookOnHold.getBookId().bookId, bookOnHold.bookInformation.bookType, UUID.randomUUID())
    }

    PatronBooksEvent.BookPlacedOnHoldByPatron bookPlacedOnHold(AvailableBook availableBook) {
        return new PatronBooksEvent.BookPlacedOnHoldByPatron(Instant.now(), UUID.randomUUID(), availableBook.getBookId().bookId, availableBook.bookInformation.bookType, UUID.randomUUID())
    }

}
