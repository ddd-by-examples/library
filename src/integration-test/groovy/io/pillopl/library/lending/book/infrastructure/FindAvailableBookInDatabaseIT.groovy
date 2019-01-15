package io.pillopl.library.lending.book.infrastructure

import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.BookId
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.library.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronBooksEvent
import io.pillopl.library.lending.patron.model.PatronId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.circulatingAvailableBookAt
import static io.pillopl.library.lending.book.model.BookType.Circulating
import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.HoldDuration.closeEnded
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHold.bookPlacedOnHoldNow
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId

@ContextConfiguration(classes = BookConfiguration.class)
@SpringBootTest
class FindAvailableBookInDatabaseIT extends Specification {

    BookId bookId = anyBookId()
    LibraryBranchId libraryBranchId = anyBranch()
    PatronId patronId = anyPatronId()

    @Autowired
    BookDatabaseRepository bookEntityRepository

    def 'should find available book in database'() {
        given:
            AvailableBook availableBook = circulatingAvailableBookAt(bookId, libraryBranchId)
        when:
            bookEntityRepository.save(availableBook)
        then:
            bookEntityRepository.findAvailableBookBy(bookId).isDefined()
        when:
            BookOnHold bookOnHold = availableBook.handle(placedOnHold())
        and:
            bookEntityRepository.save(bookOnHold)
        then:
            bookEntityRepository.findAvailableBookBy(bookId).isEmpty()
    }


    PatronBooksEvent.BookPlacedOnHold placedOnHold() {
        return events(
                bookPlacedOnHoldNow(
                bookId, Circulating,
                libraryBranchId, patronId,
                closeEnded(5)))
                .bookPlacedOnHold
    }


}
