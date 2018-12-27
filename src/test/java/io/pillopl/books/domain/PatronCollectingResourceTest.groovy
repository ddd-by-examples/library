package io.pillopl.books.domain

import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.books.domain.PatronFixture.regularPatron
import static io.pillopl.books.domain.ResourceFixture.circulatingResource

class PatronCollectingResourceTest extends Specification {

    def 'patron cannot collected resource which is not on hold'() {
        when:
            Try<Void> collect = circulatingResource().collectBy(regularPatron().getPatronId())
        then:
            !collect.isSuccess()
            ResourceCollectingFailed e = collect.getCause()
            e.message.contains("resource is not on hold")
    }

    def 'patron cannot collected resource held by another patron'() {
        given:
            Patron patron = regularPatron("patron")
            Patron anotherPatron = regularPatron("anotherPatron")
        and:
            Resource resource = circulatingResource()
        and:
            patron.hold(resource)
        when:
            Try<Void> collect = resource.collectBy(anotherPatron.getPatronId())
        then:
            !collect.isSuccess()
            ResourceCollectingFailed e = collect.getCause()
            e.message.contains("resource should be collected by the patron who put it on hold")
    }

    def 'patron can collect resource which was held by him'() {
        given:
            Patron patron = regularPatron('patron')
        and:
            Resource resource = circulatingResource()
        and:
            patron.hold(resource)
        when:
            Try<Void> collect = resource.collectBy(patron.getPatronId())
        then:
            collect.isSuccess()
            resource.isCollected()
    }

}
