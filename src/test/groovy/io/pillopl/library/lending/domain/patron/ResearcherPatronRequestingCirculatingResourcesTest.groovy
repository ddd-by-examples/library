package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.resource.Resource
import io.pillopl.library.lending.domain.resource.ResourceFixture
import io.vavr.control.Either
import spock.lang.Specification

import static io.pillopl.library.lending.domain.patron.PatronResourcesFixture.researcherPatronWithHolds
import static PatronResourcesEvent.*


class ResearcherPatronRequestingCirculatingResourcesTest extends Specification {

    def 'a researcher patron can hold any number of resources'() {
        given:
            Resource resource = ResourceFixture.circulatingResource()
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = researcherPatronWithHolds(holds).placeOnHold(resource)
        then:
            hold.isRight()
        where:
            holds << [0, 1, 2, 3, 4, 5, 100000]

    }
}
