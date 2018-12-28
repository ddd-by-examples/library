package io.pillopl.books.domain

import io.vavr.control.Either
import spock.lang.Specification

import static io.pillopl.books.domain.LibraryBranchFixture.anyBranch
import static io.pillopl.books.domain.PatronResourcesFixture.*
import static io.pillopl.books.domain.ResourceFixture.*
import static io.pillopl.books.domain.PatronResourcesEvents.*

class RegularPatronRequestingCirculatingResourcesTest extends Specification {

    //TODO: per month
    def 'a regular patron cannot place on hold more than 5 resources'() {
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatronWithHolds(holds).placeOnHold(circulatingResource())
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
            Resource resource = circulatingResource()
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
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatronWithOverdueCheckouts(overdueResources).placeOnHold(circulatingResource(libraryBranchId))
        then:
            hold.isLeft()
            ResourceHoldFailed e = hold.getLeft()
            e.reason.contains("cannot place on hold when there are overdue checkouts")
        where:
            resources << [
                    [anyResourceId(), anyResourceId()],
                    [anyResourceId(), anyResourceId(), anyResourceId()]
            ]


    }

    def 'a regular patron can place on hold resources when he doesnt have 2 overdues'() {
        given:
            Resource resource = circulatingResource()
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = regularPatronWithOverdueCheckouts(overdueResources).placeOnHold(resource)
        then:
            hold.isRight()
        where:
            overdueResources <<  [OverdueCheckouts.atBranch(anyBranch(), [anyResourceId()] as Set),
                                  OverdueCheckouts.noOverdueCheckouts()]
    }

    def 'tryinf to place a resource on hold after 4th successful consecutive holds shouldnt be possible'() {
        given:
            PatronResources patron = regularPatron()
        and:
            5.times {
                patron.placeOnHold(circulatingResource())
            }
        when:
            Either<ResourceHoldFailed, ResourcePlacedOnHold> hold = patron.placeOnHold(circulatingResource())
        then:
            hold.isLeft()
            ResourceHoldFailed e = hold.getLeft()
            e.reason.contains("patron cannot hold more resources")
    }


}
