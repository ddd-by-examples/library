package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.resource.ResourceFixture
import io.vavr.control.Either
import spock.lang.Specification

import static io.pillopl.library.lending.domain.patron.PatronResourcesFixture.regularPatron
import static PatronResourcesEvent.ResourcePlacedOnHold
import static PatronResourcesEvent.ResourceHoldFailed

class RegularPatronRequestingRestrictedResourcesTest extends Specification {

    def 'a regular patron cannot place on hold restricted resource'() {
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatron().placeOnHold(ResourceFixture.restrictedResource())
        then:
            hold.isLeft()
            ResourceHoldFailed e = hold.getLeft()
            e.reason.contains("Regular patrons cannot hold restricted resources")
    }

}
