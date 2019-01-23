package io.pillopl.library.lending.patronprofile.infrastructure

import io.pillopl.library.lending.book.model.BookId
import io.pillopl.library.lending.book.model.BookType
import io.pillopl.library.lending.dailysheet.infrastructure.SheetReadModelDatabaseConfiguration
import io.pillopl.library.lending.dailysheet.model.DailySheet
import io.pillopl.library.lending.library.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronBooksEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.PatronType
import io.pillopl.library.lending.patronprofile.model.PatronProfile
import io.pillopl.library.lending.patronprofile.model.PatronProfiles
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import javax.sql.DataSource
import java.time.Duration
import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookType.Restricted
import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId
import static io.vavr.Tuple.of
import static java.time.Instant.now

@ContextConfiguration(classes = SheetReadModelDatabaseConfiguration.class)
@SpringBootTest
class FindingPatronProfileInDatabaseIT extends Specification {

    PatronId patronId = anyPatronId()
    PatronType regular = PatronType.Regular
    LibraryBranchId libraryBranchId = anyBranch()
    BookId bookId = anyBookId()
    BookType type = Restricted

    static final Instant TOMORROW = now().plus(Duration.ofDays(1))

    @Autowired
    DataSource dataSource

    @Autowired
    DailySheet dailySheet

    @Autowired
    DataSource jdbcTemplate

    PatronProfiles patronProfiles;

    def setup() {
        patronProfiles = new PatronProfileReadModel(new JdbcTemplate(dataSource))
    }

    def 'should create patron profile'() {
        when:
            PatronProfile profile = createProfile()
        then:
            thereIsZeroHoldsAndZeroCheckouts(profile)
        when:
            dailySheet.handle(placedOnHoldTill(TOMORROW))
            profile = createProfile()
        then:
            thereIsOnlyOneHold(profile)
        when:
            dailySheet.handle(bookCollectedTill(TOMORROW))
            profile = createProfile()
        then:
            thereIsOnlyOneCheckout(profile)
        when:
            dailySheet.handle(bookReturned())
            profile = createProfile()
        then:
            thereIsZeroHoldsAndZeroCheckouts(profile)

    }

    private PatronProfile createProfile() {
        PatronProfile profile
        profile = patronProfiles.apply(patronId)
        profile
    }

    void thereIsOnlyOneHold(PatronProfile profile) {
        assert profile.holdsView.currentHolds.size() == 1
        assert profile.holdsView.currentHolds.get(0).equals(of(bookId, TOMORROW))
        assert profile.currentCheckouts.currentCheckouts.size() == 0
    }

    void thereIsOnlyOneCheckout(PatronProfile profile) {
        assert profile.holdsView.currentHolds.size() == 0
        assert profile.currentCheckouts.currentCheckouts.size() == 1
        assert profile.currentCheckouts.currentCheckouts.get(0).equals(of(bookId, TOMORROW))
    }

    void thereIsZeroHoldsAndZeroCheckouts(PatronProfile profile) {
        assert profile.holdsView.currentHolds.size() == 0
        assert profile.currentCheckouts.currentCheckouts.size() == 0

    }

    PatronBooksEvent.BookCollected bookCollectedTill(Instant till) {
        return new PatronBooksEvent.BookCollected(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                Restricted,
                libraryBranchId.getLibraryBranchId(),
                till)
    }


    PatronBooksEvent.BookPlacedOnHold placedOnHoldTill(Instant till) {
        return new PatronBooksEvent.BookPlacedOnHold(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                type,
                libraryBranchId.getLibraryBranchId(),
                now().minusSeconds(60000),
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
