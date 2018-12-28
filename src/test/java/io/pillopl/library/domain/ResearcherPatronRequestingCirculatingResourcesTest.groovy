package io.pillopl.library.domain

import io.vavr.control.Either
import spock.lang.Specification

import static PatronResourcesFixture.researcherPatronWithHolds
import static ResourceFixture.circulatingResource
import static PatronResourcesEvents.*


class ResearcherPatronRequestingCirculatingResourcesTest extends Specification {

    def 'a researcher patron can hold any number of resources'() {
        given:
            Resource resource = circulatingResource()
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = researcherPatronWithHolds(holds).placeOnHold(resource)
        then:
            hold.isRight()
        where:
            holds << [0, 1, 2, 3, 4, 5, 100000]

    }
}
