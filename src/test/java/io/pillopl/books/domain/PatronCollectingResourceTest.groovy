package io.pillopl.books.domain

import io.vavr.control.Either
import spock.lang.Specification

import static PatronResourcesFixture.regularPatron
import static io.pillopl.books.domain.ResourceFixture.circulatingResource
import static io.pillopl.books.domain.PatronResourcesEvents.*


class PatronCollectingResourceTest extends Specification {

    def 'patron cannot collected resource which is not on hold'() {
        when:
            Either<ResourceCollectingFailed, ResourceCollected> collect = regularPatron().collect(circulatingResource())
        then:
            collect.isLeft()
            ResourceCollectingFailed e = collect.getLeft()
            e.reason.contains("resource is not on hold")
    }

    def 'patron cannot collected resource held by another patron'() {
        given:
            PatronResources patron = regularPatron()
            PatronResources anotherPatron = regularPatron()
        and:
            Resource resource = circulatingResource()
        and:
            anotherPatron.hold(resource)
        when:
            Either<ResourceCollectingFailed, ResourceCollected> collect = patron.collect(resource)
        then:
            collect.isLeft()
            ResourceCollectingFailed e = collect.getLeft()
            e.reason.contains("resource is not on hold by patron")
    }

    def 'patron can collect resource which was held by him'() {
        given:
            PatronResources patron = regularPatron()
        and:
            Resource resource = circulatingResource()
        and:
            patron.hold(resource)
        when:
            Either<ResourceCollectingFailed, ResourceCollected> collect = patron.collect(resource)
        then:
            collect.isRight()
    }

}
