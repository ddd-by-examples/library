package io.pillopl.books.domain

import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.books.domain.LibraryBranchFixture.anyBranch
import static io.pillopl.books.domain.PatronFixture.*
import static io.pillopl.books.domain.ResourceFixture.*

class RegularPatronRequestingCirculatingResourcesTest extends Specification {

    def 'a regular patron cannot hold resource which is already held'() {
        when:
            Try<Void> hold = regularPatron().hold(resourceOnHold())
        then:
            !hold.isSuccess()
            ResourceHoldRequestFailed e = hold.getCause()
            e.message.contains("resource is currently not available")
    }

    def 'a regular patron cannot hold resource which is collected'() {
        when:
            Try<Void> hold = regularPatron().hold(collectedResource())
        then:
            !hold.isSuccess()
            ResourceHoldRequestFailed e = hold.getCause()
            e.message.contains("resource is currently not available")
    }

    //TODO: per month
    def 'a regular patron cannot hold more than 5 resources (not per month)'() {
        when:
            Try<Void> hold = regularPatronWithHolds(holds).hold(circulatingResource())
        then:
            !hold.isSuccess()
            ResourceHoldRequestFailed e = hold.getCause()
            e.message.contains("patron cannot hold more resources")
        where:
            holds << [5, 6, 3000]

    }

    //TODO: per month
    def 'a regular patron can book available resource when he doesnt hold more than 4 resources'() {
        given:
            Resource resource = circulatingResource()
        when:
            Try<Void> hold = regularPatronWithHolds(holds).hold(resource)
        then:
            hold.isSuccess()
            resource.isHeld()
        where:
            holds << [0, 1, 2, 3, 4]

    }

    def 'a regular patron cannot hold anymore when he has at least two overdue checkouts  '() {
        when:
            Try<Void> hold = regularPatronWithOverdueResource(overdueResources).hold(circulatingResource())
        then:
            !hold.isSuccess()
            ResourceHoldRequestFailed e = hold.getCause()
            e.message.contains("patron cannot hold in")
        where:
            overdueResources << [OverdueResources.atBranch(anyBranch(), [resourceId("123"), resourceId("456")]),
                                 OverdueResources.atBranch(anyBranch(), [resourceId("123"), resourceId("456"), resourceId("789")])]


    }

    def 'a regular patron can book available resource when he doesnt have 2 overdues'() {
        given:
            Resource resource = circulatingResource()
        when:
            Try<Void> hold = regularPatronWithOverdueResource(overdueResources).hold(resource)
        then:
            hold.isSuccess()
            resource.isHeld()
        where:
            overdueResources <<  [OverdueResources.atBranch(anyBranch(), [resourceId("123")]),
                                 OverdueResources.noOverdueResources()]
    }

    def 'fifth hold after 4th successful consecutive holds shouldnt be possible'() {
        given:
            Patron patron = regularPatron()
        and:
            5.times {
                patron.hold(circulatingResource())
            }
        when:
            Try<Void> hold = patron.hold(circulatingResource())
        then:
            !hold.isSuccess()
            ResourceHoldRequestFailed e = hold.getCause()
            e.message.contains("patron cannot hold more resources")
    }


}
