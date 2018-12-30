package io.pillopl.library.lending.domain.patron

import io.pillopl.library.lending.domain.resource.Resource
import io.pillopl.library.lending.domain.resource.ResourceFixture
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.vavr.control.Either
import spock.lang.Specification

import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronResourcesFixture.*
import static io.pillopl.library.lending.domain.patron.PatronResourcesEvents.*

class RegularPatronRequestingCirculatingResourcesTest extends Specification {

    //TODO: per month
    def 'a regular patron cannot place on hold more than 5 resources'() {
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatronWithHolds(holds).placeOnHold(ResourceFixture.circulatingResource())
        then:
            hold.isLeft()
            ResourceHoldFailed e = hold.getLeft()
            e.reason.contains("patron cannot hold more resources")
        where:
            holds << [5, 6, 3000]

    }

    //TODO: per month
    def 'a regular patron can place on hold resource when he didnt placed on hold more than 4 resources'() {
        given:
            Resource resource = ResourceFixture.circulatingResource()
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatronWithHolds(holds).placeOnHold(resource)
        then:
            hold.isRight()
        where:
            holds << [0, 1, 2, 3, 4]

    }

    def 'a regular patron cannot place on hold resources anymore when he has at least two overdue checkouts'() {
        given:
            LibraryBranchId libraryBranchId = anyBranch()
        and:
            OverdueCheckouts overdueResources = OverdueCheckouts.atBranch(libraryBranchId, resources as Set)
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatronWithOverdueCheckouts(overdueResources).placeOnHold(ResourceFixture.circulatingResource(libraryBranchId))
        then:
            hold.isLeft()
            ResourceHoldFailed e = hold.getLeft()
            e.reason.contains("cannot place on hold when there are overdue checkouts")
        where:
            resources << [
                    [ResourceFixture.anyResourceId(), ResourceFixture.anyResourceId()],
                    [ResourceFixture.anyResourceId(), ResourceFixture.anyResourceId(), ResourceFixture.anyResourceId()]
            ]


    }

    def 'a regular patron can place on hold resources when he doesnt have 2 overdues'() {
        given:
            Resource resource = ResourceFixture.circulatingResource()
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatronWithOverdueCheckouts(overdueResources).placeOnHold(resource)
        then:
            hold.isRight()
        where:
            overdueResources <<  [OverdueCheckouts.atBranch(anyBranch(), [ResourceFixture.anyResourceId()] as Set),
                                  OverdueCheckouts.noOverdueCheckouts()]
    }

    def 'tryinf to place a resource on hold after 4th successful consecutive holds shouldnt be possible'() {
        given:
            PatronResources patron = regularPatron()
        and:
            5.times {
                patron.placeOnHold(ResourceFixture.circulatingResource())
            }
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = patron.placeOnHold(ResourceFixture.circulatingResource())
        then:
            hold.isLeft()
            ResourceHoldFailed e = hold.getLeft()
            e.reason.contains("patron cannot hold more resources")
    }


}
