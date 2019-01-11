package io.pillopl.library.lending.application.hold

import io.pillopl.commons.commands.Result
import io.pillopl.library.lending.domain.patron.PatronBooks
import io.pillopl.library.lending.domain.patron.PatronBooksFixture
import io.pillopl.library.lending.domain.patron.PatronBooksRepository
import io.pillopl.library.lending.domain.patron.PatronId
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.book.BookFixture.circulatingBook
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId

class PlacingBookOnHoldTest extends Specification {

    FindAvailableBook willFindBook = { id -> Option.of(circulatingBook()) }
    FindAvailableBook willNotFindBook = { id -> Option.none() }
    PatronBooksRepository repository = Stub()

    def 'should successfully place on hold book if patron and book exist'() {
        given:
            PlacingOnHold holding = new PlacingOnHold(willFindBook, repository)
        and:
            PatronId patron = persistedRegularPatron()
        when:
            Try<Result> result = holding.placeOnHold(for3days(patron))
        then:
            result.isSuccess()
            result.get() == Result.Success

    }


    def 'should reject placing on hold book if one of the domain rules is broken (but should not fail!)'() {
        given:
            PlacingOnHold holding = new PlacingOnHold(willFindBook, repository)
        and:
            PatronId patron = persistedRegularPatronWithManyHolds()
        when:
            Try<Result> result = holding.placeOnHold(for3days(patron))
        then:
            result.isSuccess()
            result.get() == Result.Rejection

    }

    def 'should fail if patron does not exists'() {
        given:
            PlacingOnHold holding = new PlacingOnHold(willFindBook, repository)
        and:
            PatronId patron = unknownPatron()
        when:
            Try<Result> result = holding.placeOnHold(for3days(patron))
        then:
            result.isFailure()

    }


    def 'should fail if book does not exists'() {
        given:
            PlacingOnHold holding = new PlacingOnHold(willNotFindBook, repository)
        and:
            PatronId patron = persistedRegularPatron()
        when:
            Try<Result> result = holding.placeOnHold(for3days(patron))
        then:
            result.isFailure()
    }

    def 'should reject (but not fail) if saving patron fails'() {
        given:
            PlacingOnHold holding = new PlacingOnHold(willFindBook, repository)
        and:
            PatronId patron = persistedRegularPatronThatFailsOnSaving()
        when:
            Try<Result> result = holding.placeOnHold(for3days(patron))
        then:
            result.isSuccess()
            result.get() == Result.Rejection

    }

    PlaceOnHoldCommand for3days(PatronId patron) {
        return PlaceOnHoldCommand.closeEnded(patron, anyBranch(), anyBookId(), 4)
    }

    PatronId persistedRegularPatron() {
        PatronId patronId = anyPatronId()
        PatronBooks patron = PatronBooksFixture.regularPatron(patronId)
        repository.findBy(patronId) >> Option.of(patron)
        repository.handle(_) >> Try.success(patron)
        return patronId
    }

    PatronId persistedRegularPatronWithManyHolds() {
        PatronId patronId = anyPatronId()
        PatronBooks patron = PatronBooksFixture.regularPatronWithHolds(10)
        repository.findBy(patronId) >> Option.of(patron)
        return patronId
    }

    PatronId persistedRegularPatronThatFailsOnSaving() {
        PatronId patronId = anyPatronId()
        PatronBooks patron = PatronBooksFixture.regularPatron(patronId)
        repository.findBy(patronId) >> Option.of(patron)
        repository.handle(_) >> Try.failure(new IllegalStateException())
        return patronId
    }

    PatronId unknownPatron() {
        return anyPatronId()
    }

}


