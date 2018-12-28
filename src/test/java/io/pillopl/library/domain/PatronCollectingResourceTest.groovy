package io.pillopl.library.domain

import io.vavr.control.Either
import spock.lang.Specification

import static PatronResourcesFixture.regularPatron
import static ResourceFixture.circulatingResource
import static PatronResourcesEvents.*


class PatronCollectingResourceTest extends Specification {

    def 'patron cannot collect resource which is not placed on hold'() {
        when:
            Either<ResourceCollectingFailed, ResourceCollected> collect = regularPatron().collect(circulatingResource())
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
            Resource resource = circulatingResource()
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
            Resource resource = circulatingResource()
        and:
            patron.placeOnHold(resource)
        when:
            Either<ResourceCollectingFailed, ResourceCollected> collect = patron.collect(resource)
        then:
            collect.isRight()
    }

}
