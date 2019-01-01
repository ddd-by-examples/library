package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.resource.Resource;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.Value;

import java.util.function.BiFunction;

import static io.pillopl.library.lending.domain.patron.Reason.withReason;
import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

interface PlacingOnHoldPolicy extends BiFunction<Resource, PatronResources, Either<Rejection, Allowance>> {

    PlacingOnHoldPolicy onlyResearcherPatronsCanBookRestrictedResourcePolicy = (Resource toHold, PatronResources patron) -> {
        if (toHold.isRestricted() && patron.isRegular()) {
            return left(new Rejection(withReason("Regular patrons cannot hold restricted resources")));
        }
        return right(new Allowance());
    };

    PlacingOnHoldPolicy overdueCheckoutsRejectionPolicy = (Resource toHold, PatronResources patron) -> {
        final int MAX_COUNT_OF_OVERDUE_RESOURCES = 2;

        if (patron.overdueCheckoutsAt(toHold.getLibraryBranch()) >= MAX_COUNT_OF_OVERDUE_RESOURCES) {
            return left(new Rejection(withReason("cannot place on hold when there are overdue checkouts")));
        }
        return right(new Allowance());
    };

    PlacingOnHoldPolicy regularPatronMaximumNumberOfHoldsPolicy = (Resource toHold, PatronResources patron) -> {
        final int MAX_NUMBER_OF_HOLDS = 5;

        if (patron.isRegular() && patron.numberOfHolds() >= MAX_NUMBER_OF_HOLDS) {
            return left(new Rejection(withReason("patron cannot hold more resources")));
        }
        return right(new Allowance());
    };

    static List<PlacingOnHoldPolicy> allCurrentPolicies() {
        return List.of(
                onlyResearcherPatronsCanBookRestrictedResourcePolicy,
                overdueCheckoutsRejectionPolicy,
                regularPatronMaximumNumberOfHoldsPolicy);
    }

}

@Value
class Allowance { }

@Value
class Rejection {
    Reason reason;
}

@Value
class Reason {
    String reason;

    static Reason withReason(String reason) {
        return new Reason(reason);
    }
}