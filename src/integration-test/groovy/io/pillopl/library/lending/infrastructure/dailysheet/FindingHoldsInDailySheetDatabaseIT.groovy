package io.pillopl.library.lending.infrastructure.dailysheet

import io.pillopl.library.lending.domain.book.BookInformation
import io.pillopl.library.lending.domain.book.BookType
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.sql.DataSource
import java.time.Duration
import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular
import static java.time.Clock.fixed
import static java.time.Instant.now
import static java.time.ZoneId.systemDefault

@ContextConfiguration(classes = SheetReadModelDatabaseConfiguration.class)
@SpringBootTest
class FindingHoldsInDailySheetDatabaseIT extends Specification {

    PatronId patronId = anyPatronId()
    PatronInformation.PatronType regular = Regular
    LibraryBranchId libraryBranchId = anyBranch()
    BookInformation bookInformation = new BookInformation(anyBookId(), BookType.Restricted)

    static final Instant TIME_OF_EXPIRE_CHECK = now()

    @Autowired
    DataSource dataSource

    SheetsReadModel readModel

    def setup() {
        readModel = new SheetsReadModel(new JdbcTemplate(dataSource), fixed(TIME_OF_EXPIRE_CHECK, systemDefault()))
    }

    def 'should find expired holds'() {
        given:
            int currentNoOfExpiredHolds = readModel.holdsToExpireSheet().count()
        when:
            readModel.handle(placedOnHold(aCloseEndedHoldTillYesterday()))
        and:
            readModel.handle(placedOnHold(aCloseEndedHoldTillTomorrow()))
        then:
            readModel.holdsToExpireSheet().count() == currentNoOfExpiredHolds + 1
    }

    def 'handling placed on hold should de idempotent'() {
        given:
            int currentNoOfExpiredHolds = readModel.holdsToExpireSheet().count()
        and:
            PatronBooksEvent.BookPlacedOnHold event = placedOnHold(aCloseEndedHoldTillYesterday())
        when:
            2.times { readModel.handle(event) }
        then:
            readModel.holdsToExpireSheet().count() == currentNoOfExpiredHolds + 1
    }

    def 'should never find open-ended holds'() {
        given:
            int currentNoOfExpiredHolds = readModel.holdsToExpireSheet().count()
        when:
            readModel.handle(placedOnHold(anOpenEndedHold()))
        then:
            readModel.holdsToExpireSheet().count() == currentNoOfExpiredHolds
    }

    def 'should never find canceled holds'() {
        given:
            int currentNoOfExpiredHolds = readModel.holdsToExpireSheet().count()
        when:
            readModel.handle(placedOnHold(aCloseEndedHoldTillYesterday()))
        and:
            readModel.handle(holdCanceled())
        then:
            readModel.holdsToExpireSheet().count() == currentNoOfExpiredHolds
    }

    def 'should never find already expired holds'() {
        given:
            int currentNoOfExpiredHolds = readModel.holdsToExpireSheet().count()
        when:
            readModel.handle(placedOnHold(anOpenEndedHold()))
        and:
            readModel.handle(holdExpired())
        then:
            readModel.holdsToExpireSheet().count() == currentNoOfExpiredHolds
    }


    Instant aCloseEndedHoldTillTomorrow() {
        return TIME_OF_EXPIRE_CHECK.plus(Duration.ofDays(1))
    }

    Instant aCloseEndedHoldTillYesterday() {
        return TIME_OF_EXPIRE_CHECK.minus(Duration.ofDays(1))
    }

    Instant anOpenEndedHold() {
        return null
    }

    PatronBooksEvent.BookPlacedOnHold placedOnHold(Instant till) {
        return new PatronBooksEvent.BookPlacedOnHold(
                        now(),
                        patronId.getPatronId(),
                        bookInformation.getBookId().getBookId(),
                        bookInformation.bookType,
                        libraryBranchId.getLibraryBranchId(),
                        TIME_OF_EXPIRE_CHECK.minusSeconds(60000),
                        till)
    }

    PatronBooksEvent.BookHoldCanceled holdCanceled() {
        return new PatronBooksEvent.BookHoldCanceled(
                now(),
                patronId.getPatronId(),
                bookInformation.getBookId().getBookId(),
                libraryBranchId.getLibraryBranchId())
    }

    PatronBooksEvent.BookHoldExpired holdExpired() {
        return new PatronBooksEvent.BookHoldExpired(
                now(),
                patronId.getPatronId(),
                bookInformation.getBookId().getBookId(),
                libraryBranchId.getLibraryBranchId())
    }


}
