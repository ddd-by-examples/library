package io.pillopl.books.domain

import spock.lang.Specification

import static io.pillopl.books.domain.PatronFixture.regularPatron
import static io.pillopl.books.domain.ResourceFixture.restrictedResource

class RegularPatronRequestingRestrictedResourcesTest extends Specification {

    def 'a regular patron cannot hold restricted resource'() {
        when:
            regularPatron().hold(restrictedResource())
        then:
            ResourceHoldRequestFailed e = thrown(ResourceHoldRequestFailed)
            e.message.contains("Regular patrons cannot hold restricted resources")
    }

}
