package io.pillopl.library.lending.infrastructure.book

import io.pillopl.library.lending.domain.book.AvailableBook
import io.pillopl.library.lending.domain.book.BookId
import io.pillopl.library.lending.domain.book.BookInformation
import io.pillopl.library.lending.domain.book.BookOnHold
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.HoldDuration
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.book.BookFixture.circulatingAvailableBookAt
import static io.pillopl.library.lending.domain.book.BookType.Circulating
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold.now
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular

@ContextConfiguration(classes = BookDatabaseConfiguration.class)
@SpringBootTest
class FindAvailableBookInDatabaseIT extends Specification {

    BookId bookId = anyBookId()
    LibraryBranchId libraryBranchId = anyBranch()
    PatronId patronId = anyPatronId()

    @Autowired
    BookDatabaseRepository bookEntityRepository

    def 'should find available book'() {
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


    BookPlacedOnHoldEvents placedOnHold() {
        return events(
                new PatronInformation(patronId, Regular), now(
                new BookInformation(bookId, Circulating),
                libraryBranchId,
                new PatronInformation(patronId, Regular),
                HoldDuration.forCloseEnded(5)))
    }


}
