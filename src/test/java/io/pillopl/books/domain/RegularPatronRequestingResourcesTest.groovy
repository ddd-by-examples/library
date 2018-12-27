package io.pillopl.books.domain

import spock.lang.Specification

import static io.pillopl.books.domain.Resource.ResourceState.*
import static io.pillopl.books.domain.Resource.ResourceType.NORMAL

class RegularPatronRequestingResourcesTest extends Specification {

    def 'a regular patron cannot hold resource which is already held'() {
        given:
            Resource resourceOnHold = resourceOnHold()
        when:
            resourceOnHold.holdBy(Patron.regular())
        then:
            ResourceHoldRequestFailed e = thrown(ResourceHoldRequestFailed)
            e.message.contains("resource is currently ON_HOLD")
    }

    def 'a regular patron cannot hold resource which is collected'() {
        given:
            Resource collectedResource = collectedResource()
        when:
            collectedResource.holdBy(Patron.regular())
        then:
            ResourceHoldRequestFailed e = thrown(ResourceHoldRequestFailed)
            e.message.contains("resource is currently COLLECTED")
    }

    //TODO: per month
    def 'a regular patron cannot hold more than 5 resources (not per month)'() {
        given:
            Resource availableResource = availableResource()
        when:
            availableResource.holdBy(Patron.regularWithHolds(holds))
        then:
            ResourceHoldRequestFailed e = thrown(ResourceHoldRequestFailed)
            e.message.contains("patron cannot hold in")
        where:
            holds << [5, 6, 3000]

    }

    //TODO: per month
    def 'a regular patron can book available resource when he doesnt hold more than 4 resources'() {
        given:
            Resource resource = availableResource()
        when:
            resource.holdBy(Patron.regularWithHolds(holds))
        then:
            resource.isHeld()
        where:
            holds << [0, 1, 2, 3, 4]

    }

    def 'a regular patron cannot hold anymore when he has at least two overdue checkouts  '() {
        given:
            Resource availableResource = availableResource()
        when:
            availableResource.holdBy(Patron.regularWithOverdueResource(overdueResources))
        then:
            ResourceHoldRequestFailed e = thrown(ResourceHoldRequestFailed)
            e.message.contains("patron cannot hold in")
        where:
            overdueResources << [OverdueResources.atBranch(anyBranch(), [resourceId("123"), resourceId("456")]),
                                 OverdueResources.atBranch(anyBranch(), [resourceId("123"), resourceId("456"), resourceId("789")])]


    }

    def 'a regular patron can book available resource when he doesnt have 2 overdues'() {
        given:
            Resource resource = availableResource()
        when:
            resource.holdBy(Patron.regularWithOverdueResource(overdueResources))
        then:
            resource.isHeld()
        where:
            overdueResources << [OverdueResources.atBranch(anyBranch(), [resourceId("123")]),
                                 OverdueResources.noOverdueResources()]
    }

    def 'fifth hold after 4th successful consecutive holds shouldnt be possible'() {
        given:
            Patron patron = Patron.regular()
        and:
            5.times {
                availableResource().holdBy(patron)
            }
        when:
            availableResource().holdBy(patron)
        then:
            ResourceHoldRequestFailed e = thrown(ResourceHoldRequestFailed)
            e.message.contains("patron cannot hold in")
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
