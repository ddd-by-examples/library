package io.pillopl.library.domain

import io.vavr.control.Either
import spock.lang.Specification

import java.time.Instant

import static PatronResourcesFixture.regularPatron
import static ResourceFixture.circulatingResource
import static PatronResourcesEvents.*


class PatronReturningResourceTest extends Specification {

    def 'patron can return resource which is marked as placed on hold in the system'() {
        given:
            PatronResources patron = regularPatron()
            Resource resource = circulatingResource()
        and:
            patron.placeOnHold(resource)
        when:
            resource.handle(resourceReturned())
        then:
            resource.isAvailable()
    }

    def 'patron can return resource which is marked as collected in the syastem'() {
        given:
            PatronResources patron = regularPatron()
            Resource resource = circulatingResource()
        and:
            patron.placeOnHold(resource)
        and:
            patron.collect(resource)
        when:
            resource.handle(resourceReturned())
        then:
            resource.isAvailable()
    }

    def 'a patron can place on hold resource which was just returned'() {
        given:
            PatronResources patron = regularPatron()
            Resource resource = circulatingResource()
        and:
            patron.placeOnHold(resource)
        and:
            patron.collect(resource)
        when:
            resource.handle(resourceReturned())
        and:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = patron.placeOnHold(resource)
        then:
            hold.isRight()
    }

    ResourceReturned resourceReturned() {
        new ResourceReturned(Instant.now(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
    }

}
