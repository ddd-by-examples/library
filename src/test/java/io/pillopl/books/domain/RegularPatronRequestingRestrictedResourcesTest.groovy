package io.pillopl.books.domain

import io.vavr.control.Either
import spock.lang.Specification

import static PatronResourcesFixture.regularPatron
import static io.pillopl.books.domain.PatronResourcesEvents.ResourceHeld
import static io.pillopl.books.domain.PatronResourcesEvents.ResourceHoldRequestFailed
import static io.pillopl.books.domain.ResourceFixture.restrictedResource

class RegularPatronRequestingRestrictedResourcesTest extends Specification {

    def 'a regular patron cannot hold restricted resource'() {
        when:
            Either<ResourceHoldRequestFailed, ResourceHeld> hold = regularPatron().hold(restrictedResource())
        then:
            hold.isLeft()
            ResourceHoldRequestFailed e = hold.getLeft()
            e.reason.contains("Regular patrons cannot hold restricted resources")
    }

}
