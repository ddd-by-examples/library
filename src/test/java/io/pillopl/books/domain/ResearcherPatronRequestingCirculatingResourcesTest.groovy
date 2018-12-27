package io.pillopl.books.domain

import io.vavr.control.Try
import spock.lang.Specification

import static PatronResourcesFixture.researcherPatronWithHolds
import static io.pillopl.books.domain.ResourceFixture.circulatingResource

class ResearcherPatronRequestingCirculatingResourcesTest extends Specification {

    def 'a researcher patron can hold any number of resources'() {
        given:
            Resource resource = circulatingResource()
        when:
            Try<Void> hold = researcherPatronWithHolds(holds).hold(resource)
        then:
            hold.isSuccess()
            resource.isHeld()
        where:
            holds << [0, 1, 2, 3, 4, 5, 100000]

    }
}
