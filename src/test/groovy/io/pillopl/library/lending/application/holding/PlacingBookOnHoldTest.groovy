package io.pillopl.library.lending.application.holding

import io.pillopl.library.lending.application.PatronBooksFakeDatabase
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronBooksFixture
import io.pillopl.library.lending.domain.patron.PatronId
import io.vavr.control.Option
import lombok.Value
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.book.BookFixture.circulatingBook
import static io.pillopl.library.lending.domain.book.BookFixture.restrictedBook

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
        expect:
            holding.placeOnHold(anyBookId(), patron) == PlacingOnHold.Result.SUCCESS

    }

    def 'should not successfully place on hold book if trying to hold book resource (one of the domain rules is broken)'() {
        given:
            PatronId patron = persistedRegularPatron()
        and:
            PlacingOnHold holding = new PlacingOnHold(willFindRestrictedBook, repository)
        expect:
            holding.placeOnHold(anyBookId(), patron) == PlacingOnHold.Result.FAILURE

    }

    def 'should fail if patron does not exists'() {
        given:
            PatronId patron = unknownPatron()
        when:
            new PlacingOnHold(willFindBook, repository).placeOnHold(anyBookId(), patron)
        then:
            thrown(IllegalArgumentException)

    }


    def 'should fail if book does not exists'() {
        given:
            PatronId patron = persistedRegularPatron()
        when:
            new PlacingOnHold(willNotFindBook, repository).placeOnHold(anyBookId(), patron)
        then:
            thrown(IllegalArgumentException)
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
