package io.pillopl.library.lending.patron.application.hold

import io.pillopl.library.commons.commands.BatchResult
import io.pillopl.library.lending.dailysheet.model.DailySheet
import io.pillopl.library.lending.dailysheet.model.ExpiredHold
import io.pillopl.library.lending.dailysheet.model.HoldsToExpireSheet
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.Patrons
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.vavr.collection.List.of

class ExpiringHoldsTest extends Specification {

    Patrons repository = Stub()
    DailySheet dailySheet = Stub()

    PatronId patronWithExpiringHolds = anyPatronId()
    PatronId anotherPatronWithExpiringHolds = anyPatronId()

    ExpiringHolds expiring = new ExpiringHolds(dailySheet, repository)

    def setup() {
        dailySheet.queryForHoldsToExpireSheet() >> expiredHoldsBy(patronWithExpiringHolds, anotherPatronWithExpiringHolds)
    }

    def 'should return success if all holds were marked as expired'() {
        given:
            holdsWillBeExpiredSuccessfullyForBothPatrons()
        when:
            Try<BatchResult> result = expiring.expireHolds()
        then:
            result.isSuccess()
            result.get() == BatchResult.FullSuccess

    }

    def 'should return an error (but should not fail) if at least one operation failed'() {
        given:
            expiringHoldWillFailForSecondPatron()
        when:
            Try<BatchResult> result = expiring.expireHolds()
        then:
            result.isSuccess()
            result.get() == BatchResult.SomeFailed

    }

    void expiringHoldWillFailForSecondPatron() {
        repository.publish(_ as PatronEvent) >>> [null, { throw new IllegalStateException() }]
    }

    void holdsWillBeExpiredSuccessfullyForBothPatrons() {
        repository.publish(_ as PatronEvent) >> null
    }

    HoldsToExpireSheet expiredHoldsBy(PatronId patronId, PatronId anotherPatronId) {
        return new HoldsToExpireSheet(
                of(
                        new ExpiredHold(anyBookId(), patronId, anyBranch()),
                        new ExpiredHold(anyBookId(), anotherPatronId, anyBranch())
                ))
    }


}

