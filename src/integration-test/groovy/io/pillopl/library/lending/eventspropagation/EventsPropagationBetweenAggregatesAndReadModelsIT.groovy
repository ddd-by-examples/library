package io.pillopl.library.lending.eventspropagation

import io.pillopl.library.lending.book.infrastructure.BookDatabaseConfiguration
import io.pillopl.library.lending.book.model.AvailableBook
import io.pillopl.library.lending.book.model.BookFixture
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.book.model.BookRepository
import io.pillopl.library.lending.dailysheet.infrastructure.SheetReadModelDatabaseConfiguration
import io.pillopl.library.lending.library.model.LibraryBranchId
import io.pillopl.library.lending.patron.infrastructure.PatronDatabaseConfiguration
import io.pillopl.library.lending.patron.model.*
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.ColumnMapRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.sql.DataSource

import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHold.bookPlacedOnHoldNow
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHoldEvents
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.BookPlacedOnHoldEvents.events
import static io.pillopl.library.lending.patron.model.PatronBooksEvent.PatronCreated
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.regularPatron
import static io.pillopl.library.lending.patron.model.PatronInformation.PatronType.Regular

@ContextConfiguration(classes = [PatronDatabaseConfiguration.class, BookDatabaseConfiguration.class, SheetReadModelDatabaseConfiguration.class])
@SpringBootTest
class EventsPropagationBetweenAggregatesAndReadModelsIT extends Specification {

    PatronId patronId = anyPatronId()
    LibraryBranchId libraryBranchId = anyBranch()
    AvailableBook book = BookFixture.circulatingBook()

    @Autowired
    PatronBooksRepository patronBooksRepo

    @Autowired
    BookRepository bookRepository

    @Autowired
    DataSource datasource;

    def 'should synchronize PatronBooks, Book and DailySheet with events'() {
        given:
            bookRepository.save(book)
        and:
            patronBooksRepo.publish(patronCreated())
        when:
            patronBooksRepo.publish(placedOnHold(book))
        then:
            patronShouldBeFoundInDatabaseWithOneBookOnHold(patronId)
        and:
            bookReactedToPlacedOnHoldEvent()
        and:
            dailySheetIsUpdated()
    }

    boolean bookReactedToPlacedOnHoldEvent() {
        return bookRepository.findBy(book.bookId).get() instanceof BookOnHold
    }

    boolean dailySheetIsUpdated() {
        new JdbcTemplate(datasource).query("select count(*) from holds_sheet s where s.hold_by_patron_id = ?",
                [patronId.patronId] as Object[],
                new ColumnMapRowMapper()).get(0)
                .get("COUNT(*)") == 1
    }

    BookPlacedOnHoldEvents placedOnHold(AvailableBook book) {
        return events(bookPlacedOnHoldNow(
                book.bookInformation,
                book.libraryBranch,
                new PatronInformation(patronId, Regular),
                HoldDuration.closeEnded(5)))
    }

    PatronCreated patronCreated() {
        return PatronCreated.now(new PatronInformation(patronId, Regular))
    }

    void patronShouldBeFoundInDatabaseWithOneBookOnHold(PatronId patronId) {
        PatronBooks patronBooks = loadPersistedPatron(patronId)
        assert patronBooks.numberOfHolds() == 1
        assertPatronInformation(patronBooks, patronId)
    }

    void assertPatronInformation(PatronBooks patronBooks, PatronId patronId) {
        assert patronBooks.equals(regularPatron(patronId))
    }

    PatronBooks loadPersistedPatron(PatronId patronId) {
        Option<PatronBooks> loaded = patronBooksRepo.findBy(patronId)
        PatronBooks patronBooks = loaded.getOrElseThrow({
            new IllegalStateException("should have been persisted")
        })
        return patronBooks
    }
}
