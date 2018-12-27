package io.pillopl.books.domain

import spock.lang.Specification

import static io.pillopl.books.domain.PatronFixture.researcherPatronWithHolds
import static io.pillopl.books.domain.ResourceFixture.availableResource

class ResearcherPatronRequestingResourcesTest extends Specification {


    def 'a researcher patron can hold any number of resources'() {
        given:
            Resource resource = availableResource()
        when:
            resource.holdBy(researcherPatronWithHolds(holds))
        then:
            resource.isHeld()
        where:
            holds << [0, 1, 2, 3, 4, 5, 100000]

    }
}
