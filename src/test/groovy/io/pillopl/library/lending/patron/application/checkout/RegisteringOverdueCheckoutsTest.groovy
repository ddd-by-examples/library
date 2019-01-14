package io.pillopl.library.lending.patron.application.checkout

import io.pillopl.library.commons.commands.BatchResult
import io.pillopl.library.lending.dailysheet.model.CheckoutsToOverdueSheet
import io.pillopl.library.lending.dailysheet.model.DailySheet
import io.pillopl.library.lending.patron.model.PatronBooksEvent
import io.pillopl.library.lending.patron.model.PatronBooksRepository
import io.pillopl.library.lending.patron.model.PatronId
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.library.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronBooksFixture.regularPatron
import static io.vavr.collection.List.of

class RegisteringOverdueCheckoutsTest extends Specification {

    //TODO test events emitted
    PatronBooksRepository repository = Stub()
    DailySheet dailySheet = Stub()
    PatronId patronWithOverdueCheckouts = anyPatronId()
    PatronId anotherPatronWithOverdueCheckouts = anyPatronId()

    RegisteringOverdueCheckout registeringOverdueCheckout =
            new RegisteringOverdueCheckout(dailySheet, repository)

    def setup() {
        dailySheet.queryForCheckoutsToOverdue() >> overdueCheckoutsBy(patronWithOverdueCheckouts, anotherPatronWithOverdueCheckouts)
    }


    def 'should return success if all checkouts were marked as overdue'() {
        given:
            checkoutsWillBeMarkedAsOverdueForBothPatrons()
        when:
            Try<BatchResult> result = registeringOverdueCheckout.registerOverdueCheckouts()
        then:
            result.isSuccess()
            result.get() == BatchResult.FullSuccess

    }

    def 'should return an error (but should not fail) if at least one operation failed'() {
        given:
            registeringOverdueCheckoutWillFailForSecondPatron()
        when:
            Try<BatchResult> result = registeringOverdueCheckout.registerOverdueCheckouts()
        then:
            result.isSuccess()
            result.get() == BatchResult.SomeFailed

    }

    void registeringOverdueCheckoutWillFailForSecondPatron() {
        repository.publish(_ as PatronBooksEvent) >>> [regularPatron(), { throw new IllegalStateException() }]
    }

    void checkoutsWillBeMarkedAsOverdueForBothPatrons() {
        repository.publish(_ as PatronBooksEvent) >> regularPatron()
    }

    CheckoutsToOverdueSheet overdueCheckoutsBy(PatronId patronId, PatronId anotherPatronId) {
        return new CheckoutsToOverdueSheet(
                of(
                        io.vavr.Tuple.of(anyBookId(), patronId, anyBranch()),
                        io.vavr.Tuple.of(anyBookId(), anotherPatronId, anyBranch()),

                ))
    }


}

