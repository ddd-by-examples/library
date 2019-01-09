package io.pillopl.library.lending.application.expiredhold


import io.pillopl.library.lending.domain.patron.PatronBooks
import io.pillopl.library.lending.domain.patron.PatronBooksFixture
import io.pillopl.library.lending.domain.patron.PatronBooksRepository
import io.pillopl.library.lending.domain.patron.PatronId
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.vavr.collection.List.of

class ExpiringHoldsTest extends Specification {

    //TODO test events emitted
    PatronBooksRepository repository = Stub()


    def 'should return success if all holds were marked as expired'() {
        given:
            PatronId patron = persistedRegularPatron()
        and:
            PatronId anotherPatron = persistedRegularPatron()
        and:
            ExpiringHolds expiring = new ExpiringHolds( { -> existsFrom(patron, anotherPatron) }, repository)
        when:
            Try<ExpiringHolds.Result> result = expiring.expireHolds()
        then:
            result.isSuccess()
            result.get() == ExpiringHolds.Result.Success

    }

    def 'should return an error (but should not fail) if at least one operation failed'() {
        given:
            PatronId patron = persistedRegularPatron()
        and:
            PatronId anotherPatron = persistedRegularPatronThatFailsOnSaving()
        and:
            savingWillFailForSecondPatron(patron)
        and:
            ExpiringHolds expiring = new ExpiringHolds( { -> existsFrom(patron, anotherPatron) }, repository)
        when:
            Try<ExpiringHolds.Result> result = expiring.expireHolds()
        then:
            result.isSuccess()
            result.get() == ExpiringHolds.Result.Error

    }

    void savingWillFailForSecondPatron(PatronId patron) {
        repository.handle(_) >>> [Try.success(patron), Try.failure(new IllegalStateException())]
    }

    PatronId persistedRegularPatronThatFailsOnSaving() {
        PatronId patronId = anyPatronId()
        PatronBooks patron = PatronBooksFixture.regularPatron(patronId)
        repository.findBy(patronId) >> Option.of(patron)
        return patronId
    }

    PatronId persistedRegularPatron() {
        PatronId patronId = anyPatronId()
        PatronBooks patron = PatronBooksFixture.regularPatron(patronId)
        repository.findBy(patronId) >> Option.of(patron)
        return patronId
    }

    FindExpiredHolds.ExpiredHolds existsFrom(PatronId patronId, PatronId anotherPatronId) {
        return new FindExpiredHolds.ExpiredHolds(
                of(
                        io.vavr.Tuple.of(anyBookId(), patronId, anyBranch()),
                        io.vavr.Tuple.of(anyBookId(), anotherPatronId, anyBranch())

                ))
    }


}

