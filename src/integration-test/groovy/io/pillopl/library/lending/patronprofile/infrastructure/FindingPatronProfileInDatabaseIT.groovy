package io.pillopl.library.lending.patronprofile.infrastructure

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.catalogue.BookType
import io.pillopl.library.lending.LendingTestContext
import io.pillopl.library.lending.dailysheet.model.DailySheet
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.PatronType
import io.pillopl.library.lending.patronprofile.model.Checkout
import io.pillopl.library.lending.patronprofile.model.Hold
import io.pillopl.library.lending.patronprofile.model.PatronProfile
import io.pillopl.library.lending.patronprofile.model.PatronProfiles
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
import static java.time.Instant.now

@SpringBootTest(classes = LendingTestContext.class)
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
            dailySheet.handle(bookCheckedOutTill(TOMORROW))
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
        profile = patronProfiles.fetchFor(patronId)
        profile
    }

    void thereIsOnlyOneHold(PatronProfile profile) {
        assert profile.holdsView.currentHolds.size() == 1
        assert profile.holdsView.currentHolds.get(0) == new Hold(bookId, TOMORROW)
        assert profile.currentCheckouts.currentCheckouts.size() == 0
    }

    void thereIsOnlyOneCheckout(PatronProfile profile) {
        assert profile.holdsView.currentHolds.size() == 0
        assert profile.currentCheckouts.currentCheckouts.size() == 1
        assert profile.currentCheckouts.currentCheckouts.get(0) == new Checkout(bookId, TOMORROW)
    }

    void thereIsZeroHoldsAndZeroCheckouts(PatronProfile profile) {
        assert profile.holdsView.currentHolds.size() == 0
        assert profile.currentCheckouts.currentCheckouts.size() == 0

    }

    PatronEvent.BookCheckedOut bookCheckedOutTill(Instant till) {
        return new PatronEvent.BookCheckedOut(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                Restricted,
                libraryBranchId.getLibraryBranchId(),
                till)
    }


    PatronEvent.BookPlacedOnHold placedOnHoldTill(Instant till) {
        return new PatronEvent.BookPlacedOnHold(
                now(),
                patronId.getPatronId(),
                bookId.getBookId(),
                type,
                libraryBranchId.getLibraryBranchId(),
                now().minusSeconds(60000),
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
