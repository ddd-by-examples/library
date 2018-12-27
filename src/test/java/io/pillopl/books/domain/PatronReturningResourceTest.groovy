package io.pillopl.books.domain


import spock.lang.Specification

import static io.pillopl.books.domain.PatronFixture.regularPatron
import static io.pillopl.books.domain.ResourceFixture.circulatingResource

class PatronReturningResourceTest extends Specification {

    def 'patron can return resource which is on hold in the system'() {
        given:
            Patron patron = regularPatron()
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
            Patron patron = regularPatron()
            Resource resource = circulatingResource()
        and:
            patron.hold(resource)
        and:
            resource.collectBy(patron.getPatronId())
        when:
            resource.returned()
        then:
            resource.isAvailable()
    }

    def 'a patron can hold resource which was just returned'() {
        given:
            Patron patron = regularPatron()
            Resource resource = circulatingResource()
        and:
            patron.hold(resource)
        and:
            resource.collectBy(patron.getPatronId())
        when:
            resource.returned()
        and:
            patron.hold(resource)
        then:
            resource.isHeld()
    }

}
