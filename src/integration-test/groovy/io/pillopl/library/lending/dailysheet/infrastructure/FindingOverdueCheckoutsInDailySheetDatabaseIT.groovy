package io.pillopl.library.lending.dailysheet.infrastructure

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronBooksEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.PatronType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.sql.DataSource
import java.time.Duration
import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.catalogue.BookType.Restricted
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId
import static java.time.Clock.fixed
import static java.time.Instant.now
import static java.time.ZoneId.systemDefault

@ContextConfiguration(classes = SheetReadModelDatabaseConfiguration.class)
@SpringBootTest
class FindingOverdueCheckoutsInDailySheetDatabaseIT extends Specification {

    PatronId patronId = anyPatronId()
    PatronType regular = PatronType.Regular
    LibraryBranchId libraryBranchId = anyBranch()
    BookId bookId = anyBookId()

    static final Instant TIME_OF_EXPIRE_CHECK = now()

    @Autowired
    DataSource dataSource

    SheetsReadModel readModel

    def setup() {
        readModel = new SheetsReadModel(new JdbcTemplate(dataSource), fixed(TIME_OF_EXPIRE_CHECK, systemDefault()))
    }

    def 'should find overdue checkouts'() {
        given:
            int currentNoOfOverdueCheckouts = readModel.queryForCheckoutsToOverdue().count()
        when:
            readModel.handle(bookCollected(tillYesterday()))
        and:
            readModel.handle(bookCollected(tillTomorrow()))
        then:
            readModel.queryForCheckoutsToOverdue().count() == currentNoOfOverdueCheckouts + 1
    }

    def 'handling bookCollected should de idempotent'() {
        given:
            int currentNoOfOverdueCheckouts = readModel.queryForCheckoutsToOverdue().count()
        and:
            PatronBooksEvent.BookCollected event = bookCollected(tillYesterday())
        when:
            2.times { readModel.handle(event) }
        then:
            readModel.queryForCheckoutsToOverdue().count() == currentNoOfOverdueCheckouts + 1
    }

    def 'should never find returned books'() {
        given:
            int currentNoOfOverdueCheckouts = readModel.queryForCheckoutsToOverdue().count()
        and:
            readModel.handle(bookCollected(tillTomorrow()))
        when:
            readModel.handle(bookReturned())
        then:
            readModel.queryForCheckoutsToOverdue().count() == currentNoOfOverdueCheckouts
    }


    Instant tillTomorrow() {
        return TIME_OF_EXPIRE_CHECK.plus(Duration.ofDays(1))
    }

    Instant tillYesterday() {
        return TIME_OF_EXPIRE_CHECK.minus(Duration.ofDays(1))
    }

    Instant anOpenEndedHold() {
        return null
    }

    PatronBooksEvent.BookPlacedOnHold placedOnHold(Instant till) {
        return new PatronBooksEvent.BookPlacedOnHold(
                        now(),
                        patronId.getPatronId(),
                        bookId.getBookId(),
                        Restricted,
                        libraryBranchId.getLibraryBranchId(),
                        TIME_OF_EXPIRE_CHECK.minusSeconds(60000),
                        till)
    }

    PatronBooksEvent.BookHoldCanceled holdCanceled() {
        return new PatronBooksEvent.BookHoldCanceled(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                libraryBranchId.getLibraryBranchId())
    }

    PatronBooksEvent.BookHoldExpired holdExpired() {
        return new PatronBooksEvent.BookHoldExpired(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                libraryBranchId.getLibraryBranchId())
    }

    PatronBooksEvent.BookCollected bookCollected(Instant till) {
        return new PatronBooksEvent.BookCollected(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                Restricted,
                libraryBranchId.getLibraryBranchId(),
                till)
    }

    PatronBooksEvent.BookReturned bookReturned() {
        return new PatronBooksEvent.BookReturned(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                Restricted,
                libraryBranchId.getLibraryBranchId())
    }


}
