package io.pillopl.books.domain


import spock.lang.Specification

import static PatronResourcesFixture.regularPatron
import static io.pillopl.books.domain.ResourceFixture.circulatingResource

class PatronReturningResourceTest extends Specification {

    def 'patron can return resource which is on hold in the system'() {
        given:
            PatronResources patron = regularPatron()
            Resource resource = circulatingResource()
        and:
            patron.hold(resource)
        when:
            resource.returned()
        then:
            resource.isAvailable()
    }

    def 'patron can return resource which is collected'() {
        given:
            PatronResources patron = regularPatron()
            Resource resource = circulatingResource()
        and:
            patron.hold(resource)
        and:
            patron.collect(resource)
        when:
            resource.returned()
        then:
            resource.isAvailable()
    }

    def 'a patron can hold resource which was just returned'() {
        given:
            PatronResources patron = regularPatron()
            Resource resource = circulatingResource()
        and:
            patron.hold(resource)
        and:
            patron.collect(resource)
        when:
            resource.returned()
        and:
            patron.hold(resource)
        then:
            resource.isHeld()
    }

}
