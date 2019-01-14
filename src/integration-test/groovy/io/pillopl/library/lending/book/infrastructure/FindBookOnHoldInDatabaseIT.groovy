package io.pillopl.library.lending.book.infrastructure


import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.BookId
import io.pillopl.library.lending.book.model.BookInformation
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.library.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.HoldDuration
import io.pillopl.library.lending.patron.model.PatronBooksEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.PatronInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.circulatingAvailableBookAt
import static io.pillopl.library.lending.book.model.BookType.Circulating
import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHold.bookPlacedOnHoldNow
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronInformation.PatronType.Regular

@ContextConfiguration(classes = BookDatabaseConfiguration.class)
@SpringBootTest
class FindBookOnHoldInDatabaseIT extends Specification {

    BookId bookId = anyBookId()
    LibraryBranchId libraryBranchId = anyBranch()
    PatronId patronId = anyPatronId()

    @Autowired
    BookDatabaseRepository bookEntityRepository

    def 'should find book on hold in database'() {
        given:
            AvailableBook availableBook = circulatingAvailableBookAt(bookId, libraryBranchId)
        when:
            bookEntityRepository.save(availableBook)
        then:
            bookEntityRepository.findBookOnHold(bookId, patronId).isEmpty()
        when:
            BookOnHold bookOnHold = availableBook.handle(placedOnHoldBy(patronId))
        and:
            bookEntityRepository.save(bookOnHold)
        then:
            bookEntityRepository.findBookOnHold(bookId, patronId).isDefined()
    }

    PatronBooksEvent.BookPlacedOnHold placedOnHoldBy(PatronId patronId) {
        return events(bookPlacedOnHoldNow(
                new BookInformation(bookId, Circulating),
                libraryBranchId,
                new PatronInformation(patronId, Regular),
                HoldDuration.closeEnded(5))).bookPlacedOnHold
    }

}
