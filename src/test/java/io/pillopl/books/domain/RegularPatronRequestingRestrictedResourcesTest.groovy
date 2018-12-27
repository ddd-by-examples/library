package io.pillopl.books.domain

import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.books.domain.PatronFixture.regularPatron
import static io.pillopl.books.domain.ResourceFixture.restrictedResource

class RegularPatronRequestingRestrictedResourcesTest extends Specification {

    def 'a regular patron cannot hold restricted resource'() {
        when:
            Try<Void> hold = regularPatron().hold(restrictedResource())
        then:
            !hold.isSuccess()
            ResourceHoldRequestFailed e = hold.getCause()
            e.message.contains("Regular patrons cannot hold restricted resources")
    }

}
