package io.pillopl.library.lending.patron.application.checkout

import io.pillopl.library.commons.commands.BatchResult
import io.pillopl.library.lending.dailysheet.model.CheckoutsToOverdueSheet
import io.pillopl.library.lending.dailysheet.model.DailySheet
import io.pillopl.library.lending.dailysheet.model.OverdueCheckout
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.Patrons
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronFixture.regularPatron
import static io.vavr.collection.List.of

class RegisteringOverdueCheckoutsTest extends Specification {

    Patrons repository = Stub()
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
        repository.publish(_ as PatronEvent) >>> [regularPatron(), { throw new IllegalStateException() }]
    }

    void checkoutsWillBeMarkedAsOverdueForBothPatrons() {
        repository.publish(_ as PatronEvent) >> regularPatron()
    }

    CheckoutsToOverdueSheet overdueCheckoutsBy(PatronId patronId, PatronId anotherPatronId) {
        return new CheckoutsToOverdueSheet(
                of(
                        new OverdueCheckout(anyBookId(), patronId, anyBranch()),
                        new OverdueCheckout(anyBookId(), anotherPatronId, anyBranch()),

                ))
    }


}

