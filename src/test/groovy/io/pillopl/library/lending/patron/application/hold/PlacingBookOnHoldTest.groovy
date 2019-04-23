package io.pillopl.library.lending.patron.application.hold


import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.patron.model.Patron
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronFixture
import io.pillopl.library.lending.patron.model.Patrons
import io.pillopl.library.lending.patron.model.PatronId
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.circulatingBook
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronFixture.regularPatron

class PlacingBookOnHoldTest extends Specification {

    FindAvailableBook willFindBook = { id -> Option.of(circulatingBook()) }
    FindAvailableBook willNotFindBook = { id -> Option.none() }
    Patrons repository = Stub()

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

    def 'should fail if saving patron fails'() {
        given:
            PlacingOnHold holding = new PlacingOnHold(willFindBook, repository)
        and:
            PatronId patron = persistedRegularPatronThatFailsOnSaving()
        when:
            Try<Result> result = holding.placeOnHold(for3days(patron))
        then:
            result.isFailure()

    }

    PlaceOnHoldCommand for3days(PatronId patron) {
        return PlaceOnHoldCommand.closeEnded(patron, anyBranch(), anyBookId(), 4)
    }

    PatronId persistedRegularPatron() {
        PatronId patronId = anyPatronId()
        Patron patron = regularPatron(patronId)
        repository.findBy(patronId) >> Option.of(patron)
        repository.publish(_ as PatronEvent) >> patron
        return patronId
    }

    PatronId persistedRegularPatronWithManyHolds() {
        PatronId patronId = anyPatronId()
        Patron patron = PatronFixture.regularPatronWithHolds(10)
        repository.findBy(patronId) >> Option.of(patron)
        repository.publish(_ as PatronEvent) >> patron

        return patronId
    }

    PatronId persistedRegularPatronThatFailsOnSaving() {
        PatronId patronId = anyPatronId()
        Patron patron = regularPatron(patronId)
        repository.findBy(patronId) >> Option.of(patron)
        repository.publish(_ as PatronEvent) >> {throw new IllegalStateException()}
        return patronId
    }

    PatronId unknownPatron() {
        return anyPatronId()
    }

}


