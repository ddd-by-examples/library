package io.pillopl.books.domain

import io.vavr.control.Either
import spock.lang.Specification

import static PatronResourcesFixture.regularPatron
import static io.pillopl.books.domain.PatronResourcesEvents.ResourcePlacedOnHold
import static io.pillopl.books.domain.PatronResourcesEvents.ResourceHoldFailed
import static io.pillopl.books.domain.ResourceFixture.restrictedResource

class RegularPatronRequestingRestrictedResourcesTest extends Specification {

    def 'a regular patron cannot place on hold restricted resource'() {
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatron().placeOnHold(restrictedResource())
        then:
            hold.isLeft()
            ResourceHoldFailed e = hold.getLeft()
            e.reason.contains("Regular patrons cannot hold restricted resources")
    }

}
