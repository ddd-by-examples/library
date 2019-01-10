package io.pillopl.library.lending.application.hold

import io.pillopl.library.lending.domain.dailysheet.DailySheet
import io.pillopl.library.lending.domain.dailysheet.HoldsToExpireSheet
import io.pillopl.library.lending.domain.patron.PatronBooksRepository
import io.pillopl.library.lending.domain.patron.PatronId
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.vavr.collection.List.of

class ExpiringHoldsTest extends Specification {

    //TODO test events emitted
    PatronBooksRepository repository = Stub()
    DailySheet dailySheet = Stub()

    PatronId patronWithExpiringHolds = anyPatronId()
    PatronId anotherPatronWithExpiringHolds = anyPatronId()

    ExpiringHolds expiring = new ExpiringHolds(dailySheet, repository)

    def setup() {
        dailySheet.holdsToExpireSheet() >> expiredHoldsBy(patronWithExpiringHolds, anotherPatronWithExpiringHolds)
    }

    def 'should return success if all holds were marked as expired'() {
        given:
            holdsWillBeExpiredSuccessfullyForBothPatrons()
        when:
            Try<ExpiringHolds.Result> result = expiring.expireHolds()
        then:
            result.isSuccess()
            result.get() == ExpiringHolds.Result.Success

    }

    def 'should return an error (but should not fail) if at least one operation failed'() {
        given:
            expiringHoldWillFailForSecondPatron()
        when:
            Try<ExpiringHolds.Result> result = expiring.expireHolds()
        then:
            result.isSuccess()
            result.get() == ExpiringHolds.Result.Error

    }

    void expiringHoldWillFailForSecondPatron() {
        repository.handle(_) >>> [Try.success(null), Try.failure(new IllegalStateException())]
    }

    void holdsWillBeExpiredSuccessfullyForBothPatrons() {
        repository.handle(_) >> Try.success(null)
    }

    HoldsToExpireSheet expiredHoldsBy(PatronId patronId, PatronId anotherPatronId) {
        return new HoldsToExpireSheet(
                of(
                        io.vavr.Tuple.of(anyBookId(), patronId, anyBranch()),
                        io.vavr.Tuple.of(anyBookId(), anotherPatronId, anyBranch())

                ))
    }


}

