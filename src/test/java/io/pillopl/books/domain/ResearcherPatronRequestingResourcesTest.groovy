package io.pillopl.books.domain

import spock.lang.Specification

import static io.pillopl.books.domain.Resource.ResourceState.*
import static io.pillopl.books.domain.Resource.ResourceType.NORMAL

class ResearcherPatronRequestingResourcesTest extends Specification {


    def 'a researcher patron can hold any number of resources'() {
        given:
            Resource resource = availableResource()
        when:
            resource.holdBy(Patron.researcherWithHolds(holds))
        then:
            resource.isHeld()
        where:
            holds << [0, 1, 2, 3, 4, 5, 100000]

    }

    ResourceId resourceId(String resourceId) {
        return new ResourceId(resourceId)
    }

    Resource resourceOnHold() {
        return new Resource(anyBranch(), NORMAL, ON_HOLD)
    }

    Resource availableResource() {
        return new Resource(anyBranch(), NORMAL, AVAILABLE)
    }

    Resource collectedResource() {
        return new Resource(anyBranch(), NORMAL, COLLECTED)
    }

    LibraryBranchId anyBranch() {
        return new LibraryBranchId()
    }
}
