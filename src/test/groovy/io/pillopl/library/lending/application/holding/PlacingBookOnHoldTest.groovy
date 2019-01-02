package io.pillopl.library.lending.application.holding

import io.pillopl.library.lending.application.PatronBooksFakeDatabase
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronBooksFixture
import io.pillopl.library.lending.domain.patron.PatronId
import io.vavr.control.Option
import io.vavr.control.Try
import lombok.Value
import spock.lang.Specification

import static io.pillopl.library.lending.application.holding.PlacingOnHold.Result.Rejection
import static io.pillopl.library.lending.application.holding.PlacingOnHold.Result.Success
import static io.pillopl.library.lending.domain.book.BookFixture.*

class PlacingBookOnHoldTest extends Specification {

    PatronBooksFakeDatabase repository = new PatronBooksFakeDatabase()
    FindBook willFindBook = { id -> Option.of(circulatingBook()) }
    FindBook willNotFindBook = { id -> Option.none() }
    FindBook willFindRestrictedBook = { id -> Option.of(restrictedBook()) }

    def 'should successfully place on hold book if patron and book exist'() {
        given:
            PatronId patron = persistedRegularPatron()
        and:
            PlacingOnHold holding = new PlacingOnHold(willFindBook, repository)
        when:
            Try<PlacingOnHold.Result> result = holding.placeOnHold(anyBookId(), patron)
        then:
            result.isSuccess()
            result.get() == Success

    }

    def 'should reject placing on hold book if trying to hold book resource (one of the domain rules is broken)'() {
        given:
            PatronId patron = persistedRegularPatron()
        and:
            PlacingOnHold holding = new PlacingOnHold(willFindRestrictedBook, repository)
        when:
            Try<PlacingOnHold.Result> result = holding.placeOnHold(anyBookId(), patron)
        then:
            result.isSuccess()
            result.get() == Rejection

    }

    def 'should fail if patron does not exists'() {
        given:
            PatronId patron = unknownPatron()
        when:
            Try<PlacingOnHold.Result> result = new PlacingOnHold(willFindBook, repository).placeOnHold(anyBookId(), patron)
        then:
            result.isFailure()

    }


    def 'should fail if book does not exists'() {
        given:
            PatronId patron = persistedRegularPatron()
        when:
            Try<PlacingOnHold.Result> result = new PlacingOnHold(willNotFindBook, repository).placeOnHold(anyBookId(), patron)
        then:
            result.isFailure()
    }

    PatronId persistedRegularPatron() {
        PatronId patronId = PatronBooksFixture.anyPatronId();
        repository.reactTo(new FakePatronCreatedEvent(patronId))
        return patronId
    }

    PatronId unknownPatron() {
        return PatronBooksFixture.anyPatronId();
    }
}

@Value
class FakePatronCreatedEvent implements PatronBooksEvent {

    FakePatronCreatedEvent(PatronId patronId) {
        this.patronId = patronId.patronId
    }

    UUID patronId;

}
