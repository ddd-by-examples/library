package io.pillopl.library.lending.patron.application.hold

import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.book.model.BookOnHold

import io.pillopl.library.lending.patron.model.*
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.bookOnHold
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId

class CancelingHoldTest extends Specification {

    BookOnHold bookOnHold = bookOnHold()
    PatronId patronId = anyPatronId()

    FindBookOnHold willFindBook = { bookId, patronId -> Option.of(bookOnHold) }
    FindBookOnHold willNotFindBook = { bookId, patronId -> Option.none() }
    Patrons repository = Stub()

    def 'should successfully cancel hold if book was placed on hold by patron and patron and book exist'() {
        given:
            CancelingHold canceling = new CancelingHold(willFindBook, repository)
        and:
            persistedRegularPatronWithBookOnHold()
        when:
            Try<Result> result = canceling.cancelHold(cmd())
        then:
            result.isSuccess()
            result.get() == Result.Success
    }

    def 'should reject placing on hold book if one of the domain rules is broken (but should not fail!)'() {
        given:
            CancelingHold canceling = new CancelingHold(willFindBook, repository)
        and:
            persistedRegularPatronWithoutBookOnHold()
        when:
            Try<Result> result = canceling.cancelHold(cmd())
        then:
            result.isSuccess()
            result.get() == Result.Rejection

    }

    def 'should fail if patron does not exists'() {
        given:
            CancelingHold canceling = new CancelingHold(willFindBook, repository)
        and:
            unknownPatron()
        when:
            Try<Result> result = canceling.cancelHold(cmd())
        then:
            result.isFailure()

    }

    def 'should fail if book does not exists'() {
        given:
            CancelingHold canceling = new CancelingHold(willNotFindBook, repository)
        and:
            persistedRegularPatronWithBookOnHold()
        when:
            Try<Result> result = canceling.cancelHold(cmd())
        then:
            result.isFailure()
    }

    def 'should fail if saving patron fails'() {
        given:
            CancelingHold canceling = new CancelingHold(willFindBook, repository)
        and:
            persistedRegularPatronThatFailsOnSaving()
        when:
            Try<Result> result = canceling.cancelHold(cmd())
        then:
            result.isFailure()

    }

    CancelHoldCommand cmd() {
        return new CancelHoldCommand(Instant.now(), patronId, anyBookId())
    }

    PatronId persistedRegularPatronWithBookOnHold() {
        Patron patron = PatronFixture.regularPatronWithHold(bookOnHold)
        repository.findBy(patronId) >> Option.of(patron)
        repository.publish(_ as PatronEvent) >> patron
        return patronId
    }

    PatronId persistedRegularPatronWithoutBookOnHold() {
        Patron patron = PatronFixture.regularPatronWithHolds(10)
        repository.findBy(patronId) >> Option.of(patron)
        return patronId
    }

    PatronId persistedRegularPatronThatFailsOnSaving() {
        Patron patron = PatronFixture.regularPatronWithHold(bookOnHold)
        repository.findBy(patronId) >> Option.of(patron)
        repository.publish(_ as PatronEvent) >> { throw new IllegalStateException() }
        return patronId
    }

    PatronId unknownPatron() {
        return anyPatronId()
    }

}


