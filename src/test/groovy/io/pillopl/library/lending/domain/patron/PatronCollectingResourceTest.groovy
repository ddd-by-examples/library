package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.resource.Resource
import io.pillopl.library.lending.domain.resource.ResourceFixture
import io.vavr.control.Either
import spock.lang.Specification

import static PatronResourcesEvent.ResourceCollected
import static PatronResourcesEvent.ResourceCollectingFailed
import static io.pillopl.library.lending.domain.patron.PatronResourcesFixture.regularPatron

class PatronCollectingResourceTest extends Specification {

    def 'patron cannot collect resource which is not placed on hold'() {
        when:
            Either<ResourceCollectingFailed, ResourceCollected> collect = regularPatron().collect(ResourceFixture.circulatingResource())
        then:
            collect.isLeft()
            ResourceCollectingFailed e = collect.getLeft()
            e.reason.contains("resource is not on hold")
    }

    def 'patron cannot collect resource which is placed on hold by another patron'() {
        given:
            PatronResources patron = regularPatron()
            PatronResources anotherPatron = regularPatron()
        and:
            Resource resource = ResourceFixture.circulatingResource()
        and:
            anotherPatron.placeOnHold(resource)
        when:
            Either<ResourceCollectingFailed, ResourceCollected> collect = patron.collect(resource)
        then:
            collect.isLeft()
            ResourceCollectingFailed e = collect.getLeft()
            e.reason.contains("resource is not on hold by patron")
    }

    def 'patron can collect resource which was placed on hold by him'() {
        given:
            PatronResources patron = regularPatron()
        and:
            Resource resource = ResourceFixture.circulatingResource()
        and:
            patron.placeOnHold(resource)
        when:
            Either<ResourceCollectingFailed, ResourceCollected> collect = patron.collect(resource)
        then:
            collect.isRight()
    }

}
