package io.pillopl.library.lending.dailysheet.infrastructure

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.lending.LendingTestContext
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.PatronType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

import javax.sql.DataSource
import java.time.Duration
import java.time.Instant

import static io.pillopl.library.catalogue.BookType.Restricted
import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static java.time.Clock.fixed
import static java.time.Instant.now
import static java.time.ZoneId.systemDefault

@SpringBootTest(classes = LendingTestContext.class)
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
            readModel.handle(bookCheckedOut(tillYesterday()))
        and:
            readModel.handle(bookCheckedOut(tillTomorrow()))
        then:
            readModel.queryForCheckoutsToOverdue().count() == currentNoOfOverdueCheckouts + 1
    }

    def 'handling bookCheckedOut should de idempotent'() {
        given:
            int currentNoOfOverdueCheckouts = readModel.queryForCheckoutsToOverdue().count()
        and:
		PatronEvent.BookCheckedOut event = bookCheckedOut(tillYesterday())
        when:
            2.times { readModel.handle(event) }
        then:
            readModel.queryForCheckoutsToOverdue().count() == currentNoOfOverdueCheckouts + 1
    }

    def 'should never find returned books'() {
        given:
            int currentNoOfOverdueCheckouts = readModel.queryForCheckoutsToOverdue().count()
        and:
            readModel.handle(bookCheckedOut(tillTomorrow()))
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

    PatronEvent.BookPlacedOnHold placedOnHold(Instant till) {
        return new PatronEvent.BookPlacedOnHold(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                Restricted,
                libraryBranchId.getLibraryBranchId(),
                TIME_OF_EXPIRE_CHECK.minusSeconds(60000),
                till)
    }

    PatronEvent.BookHoldCanceled holdCanceled() {
        return new PatronEvent.BookHoldCanceled(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                libraryBranchId.getLibraryBranchId())
    }

    PatronEvent.BookHoldExpired holdExpired() {
        return new PatronEvent.BookHoldExpired(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                libraryBranchId.getLibraryBranchId())
    }

	PatronEvent.BookCheckedOut bookCheckedOut(Instant till) {
        return new PatronEvent.BookCheckedOut(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                Restricted,
                libraryBranchId.getLibraryBranchId(),
                till)
    }

    PatronEvent.BookReturned bookReturned() {
        return new PatronEvent.BookReturned(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                Restricted,
                libraryBranchId.getLibraryBranchId())
    }

}