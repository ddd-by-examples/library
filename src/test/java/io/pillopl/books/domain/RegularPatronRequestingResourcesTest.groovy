package io.pillopl.books.domain

import spock.lang.Specification

import static io.pillopl.books.domain.LibraryBranchFixture.anyBranch
import static io.pillopl.books.domain.PatronFixture.regularPatron
import static io.pillopl.books.domain.ResourceFixture.availableResource
import static io.pillopl.books.domain.ResourceFixture.collectedResource
import static io.pillopl.books.domain.ResourceFixture.resourceId
import static io.pillopl.books.domain.ResourceFixture.resourceOnHold

class RegularPatronRequestingResourcesTest extends Specification {

    def 'a regular patron cannot hold resource which is already held'() {
        given:
            Resource resourceOnHold = resourceOnHold()
        when:
            resourceOnHold.holdBy(regularPatron())
        then:
            ResourceHoldRequestFailed e = thrown(ResourceHoldRequestFailed)
            e.message.contains("resource is currently ON_HOLD")
    }

    def 'a regular patron cannot hold resource which is collected'() {
        given:
            Resource collectedResource = collectedResource()
        when:
            collectedResource.holdBy(regularPatron())
        then:
            ResourceHoldRequestFailed e = thrown(ResourceHoldRequestFailed)
            e.message.contains("resource is currently COLLECTED")
    }

    //TODO: per month
    def 'a regular patron cannot hold more than 5 resources (not per month)'() {
        given:
            Resource availableResource = availableResource()
        when:
            availableResource.holdBy(PatronFixture.regularPatronWithHolds(holds))
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
            resource.holdBy(PatronFixture.regularPatronWithHolds(holds))
        then:
            resource.isHeld()
        where:
            holds << [0, 1, 2, 3, 4]

    }

    def 'a regular patron cannot hold anymore when he has at least two overdue checkouts  '() {
        given:
            Resource availableResource = availableResource()
        when:
            availableResource.holdBy(PatronFixture.regularPatronWithOverdueResource(overdueResources))
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
            resource.holdBy(PatronFixture.regularPatronWithOverdueResource(overdueResources))
        then:
            resource.isHeld()
        where:
            overdueResources << [OverdueResources.atBranch(anyBranch(), [resourceId("123")]),
                                 OverdueResources.noOverdueResources()]
    }

    def 'fifth hold after 4th successful consecutive holds shouldnt be possible'() {
        given:
            Patron patron = regularPatron()
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


}
