package io.pillopl.books.domain

import io.vavr.control.Either
import spock.lang.Specification

import static io.pillopl.books.domain.LibraryBranchFixture.anyBranch
import static io.pillopl.books.domain.PatronResourcesFixture.*
import static io.pillopl.books.domain.ResourceFixture.*
import static io.pillopl.books.domain.PatronResourcesEvents.*

class RegularPatronRequestingCirculatingResourcesTest extends Specification {

    //TODO: per month
    def 'a regular patron cannot hold more than 5 resources (not per month)'() {
        when:
            Either<ResourceHoldRequestFailed, ResourceHeld> hold = regularPatronWithHolds(holds).hold(circulatingResource())
        then:
            hold.isLeft()
            ResourceHoldRequestFailed e = hold.getLeft()
            e.reason.contains("patron cannot hold more resources")
        where:
            holds << [5, 6, 3000]

    }

    //TODO: per month
    def 'a regular patron can book available resource when he doesnt hold more than 4 resources'() {
        given:
            Resource resource = circulatingResource()
        when:
            Either<ResourceHoldRequestFailed, ResourceHeld> hold = regularPatronWithHolds(holds).hold(resource)
        then:
            hold.isRight()
        where:
            holds << [0, 1, 2, 3, 4]

    }

    def 'a regular patron cannot hold anymore when he has at least two overdue checkouts  '() {
        given:
            LibraryBranchId libraryBranchId = anyBranch()
        and:
            OverdueResources overdueResources = OverdueResources.atBranch(libraryBranchId, resources)
        when:
            Either<ResourceHoldRequestFailed, ResourceHeld> hold = regularPatronWithOverdueResource(overdueResources).hold(circulatingResource(libraryBranchId))
        then:
            hold.isLeft()
            ResourceHoldRequestFailed e = hold.getLeft()
            e.reason.contains("patron cannot hold in")
        where:
            resources << [
                    [anyResourceId(), anyResourceId()],
                    [anyResourceId(), anyResourceId(), anyResourceId()]
            ]


    }

    def 'a regular patron can book available resource when he doesnt have 2 overdues'() {
        given:
            Resource resource = circulatingResource()
        when:
            Either<ResourceHoldRequestFailed, ResourceHeld> hold = regularPatronWithOverdueResource(overdueResources).hold(resource)
        then:
            hold.isRight()
        where:
            overdueResources <<  [OverdueResources.atBranch(anyBranch(), [anyResourceId()]),
                                 OverdueResources.noOverdueResources()]
    }

    def 'fifth hold after 4th successful consecutive holds shouldnt be possible'() {
        given:
            PatronResources patron = regularPatron()
        and:
            5.times {
                patron.hold(circulatingResource())
            }
        when:
            Either<ResourceHoldRequestFailed, ResourceHeld> hold = patron.hold(circulatingResource())
        then:
            hold.isLeft()
            ResourceHoldRequestFailed e = hold.getLeft()
            e.reason.contains("patron cannot hold more resources")
    }


}
