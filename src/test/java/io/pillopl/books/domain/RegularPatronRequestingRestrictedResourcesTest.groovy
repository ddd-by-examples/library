package io.pillopl.books.domain

import spock.lang.Specification

import static io.pillopl.books.domain.Resource.ResourceState.AVAILABLE
import static io.pillopl.books.domain.Resource.ResourceType.RESTRICTED

class RegularPatronRequestingRestrictedResourcesTest extends Specification {

    def 'a regular patron cannot hold restricted resource'() {
        given:
            Resource restrictedResource = restrictedResource()
        when:
            restrictedResource.holdBy(Patron.regular())
        then:
            ResourceHoldRequestFailed e = thrown(ResourceHoldRequestFailed)
            e.message.contains("Regular patrons cannot hold restricted resources")
    }

    Resource restrictedResource() {
        return new Resource(anyBranch(), RESTRICTED, AVAILABLE)
    }

    LibraryBranchId anyBranch() {
        return new LibraryBranchId()
    }

}
