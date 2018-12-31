package io.pillopl.library.lending.application.holding

import io.pillopl.library.lending.application.InMemoryPatronResourcesRepository
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronResources
import io.pillopl.library.lending.domain.patron.PatronResourcesFixture
import io.vavr.control.Option
import spock.lang.Specification

import static io.pillopl.library.lending.domain.resource.ResourceFixture.*

class PlacingOnHoldTest extends Specification {

    InMemoryPatronResourcesRepository repository = new InMemoryPatronResourcesRepository()
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

    def 'persistence works (trying to hold more than 5 resources requires persistence mechanism to work)'() {
        given:
            PatronId patron = persistedRegularPatron()
        and:
            PlacingOnHold holding = new PlacingOnHold(willFindResource, repository)
        and:
            5.times {
                holding.placeOnHold(anyResourceId(), patron)
            }
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
        PatronResources patron = PatronResourcesFixture.regularPatron(patronId)
        repository.save(patron)
        return patronId
    }

    PatronId unknownPatron() {
        PatronId patronId = PatronResourcesFixture.anyPatronId();
        PatronResources patron = PatronResourcesFixture.regularPatron(patronId)
        return patronId
    }
}
