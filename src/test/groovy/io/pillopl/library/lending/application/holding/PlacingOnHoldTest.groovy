package io.pillopl.library.lending.application.holding

import io.pillopl.library.lending.application.FakePatronResourcesRepository
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronResourcesEvent
import io.pillopl.library.lending.domain.patron.PatronResourcesFixture
import io.vavr.control.Option
import lombok.Value
import spock.lang.Specification

import static io.pillopl.library.lending.domain.resource.ResourceFixture.*

class PlacingOnHoldTest extends Specification {

    FakePatronResourcesRepository repository = new FakePatronResourcesRepository()
    FindResource willFindResource = { id -> Option.of(circulatingResource()) }
    FindResource willNotFindResource = { id -> Option.none() }
    FindResource willFindRestrictedResource = { id -> Option.of(restrictedResource()) }

    def 'should successfully place on hold resource if patron and resource exists'() {
        given:
            PatronId patron = persistedRegularPatron()
        and:
            PlacingOnHold holding = new PlacingOnHold(willFindResource, repository)
        expect:
            holding.placeOnHold(anyResourceId(), patron) == PlacingOnHold.Result.SUCCESS

    }

    def 'should not successfully place on hold resource if trying to hold restricted resource (one of the domain rules is broken)'() {
        given:
            PatronId patron = persistedRegularPatron()
        and:
            PlacingOnHold holding = new PlacingOnHold(willFindRestrictedResource, repository)
        expect:
            holding.placeOnHold(anyResourceId(), patron) == PlacingOnHold.Result.FAILURE

    }

    def 'should fail if patron does not exists'() {
        given:
            PatronId patron = unknownPatron()
        when:
            new PlacingOnHold(willFindResource, repository).placeOnHold(anyResourceId(), patron)
        then:
            thrown(IllegalArgumentException)

    }


    def 'should fail if resource does not exists'() {
        given:
            PatronId patron = persistedRegularPatron()
        when:
            new PlacingOnHold(willNotFindResource, repository).placeOnHold(anyResourceId(), patron)
        then:
            thrown(IllegalArgumentException)
    }

    PatronId persistedRegularPatron() {
        PatronId patronId = PatronResourcesFixture.anyPatronId();
        repository.reactTo(new FakePatronCreatedEvent(patronId))
        return patronId
    }

    PatronId unknownPatron() {
        return PatronResourcesFixture.anyPatronId();
    }
}

@Value
class FakePatronCreatedEvent implements PatronResourcesEvent {

    FakePatronCreatedEvent(PatronId patronId) {
        this.patronId = patronId.patronId
    }

    UUID patronId;

}
