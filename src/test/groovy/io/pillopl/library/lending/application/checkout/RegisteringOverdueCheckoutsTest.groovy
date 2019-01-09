package io.pillopl.library.lending.application.checkout


import io.pillopl.library.lending.domain.patron.PatronBooksRepository
import io.pillopl.library.lending.domain.patron.PatronId
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.vavr.collection.List.of

class RegisteringOverdueCheckoutsTest extends Specification {

    //TODO test events emitted
    PatronBooksRepository repository = Stub()
    PatronId patronWithOverdueCheckouts = anyPatronId()
    PatronId anotherPatronWithOverdueCheckouts = anyPatronId()

    RegisteringOverdueCheckout registeringOverdueCheckout =
            new RegisteringOverdueCheckout({ ->
                overdueCheckoutsBy(patronWithOverdueCheckouts, anotherPatronWithOverdueCheckouts)
            }, repository)


    def 'should return success if all checkouts were marked as overdue'() {
        given:
            checkoutsWillBeMarkedAsOverdueForBothPatrons()
        when:
            Try<RegisteringOverdueCheckout.Result> result = registeringOverdueCheckout.registerOverdueCheckouts()
        then:
            result.isSuccess()
            result.get() == RegisteringOverdueCheckout.Result.Success

    }

    def 'should return an error (but should not fail) if at least one operation failed'() {
        given:
            registeringOverdueCheckoutWillFailForSecondPatron()
        when:
            Try<RegisteringOverdueCheckout.Result> result = registeringOverdueCheckout.registerOverdueCheckouts()
        then:
            result.isSuccess()
            result.get() == RegisteringOverdueCheckout.Result.Error

    }

    void registeringOverdueCheckoutWillFailForSecondPatron() {
        repository.handle(_) >>> [Try.success(null), Try.failure(new IllegalStateException())]
    }

    void checkoutsWillBeMarkedAsOverdueForBothPatrons() {
        repository.handle(_) >> Try.success(null)
    }


    FindCheckoutsToOverduePolicy.CheckoutsToMarkAsOverdueView overdueCheckoutsBy(PatronId patronId, PatronId anotherPatronId) {
        return new FindCheckoutsToOverduePolicy.CheckoutsToMarkAsOverdueView(
                of(
                        io.vavr.Tuple.of(anyBookId(), patronId),
                        io.vavr.Tuple.of(anyBookId(), anotherPatronId)

                ))
    }


}

