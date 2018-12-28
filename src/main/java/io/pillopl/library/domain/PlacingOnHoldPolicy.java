package io.pillopl.library.domain;

import io.vavr.control.Either;
import lombok.Value;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

interface PlacingOnHoldPolicy {

    Either<Rejection, Allowance> canPlaceOnHold(Resource toHold, PatronResources by);

}

class OnlyResearcherPatronsCanBookRestrictedResourcePolicy implements PlacingOnHoldPolicy {

    @Override
    public Either<Rejection, Allowance> canPlaceOnHold(Resource toHold, PatronResources patron) {
        if (toHold.isRestricted() && patron.isRegular()) {
            return left(new Rejection("Regular patrons cannot hold restricted resources"));
        }
        return right(new Allowance());
    }
}

class OverdueCheckoutsRejectionPolicy implements PlacingOnHoldPolicy {

    @Override
    public Either<Rejection, Allowance> canPlaceOnHold(Resource toHold, PatronResources patron) {

        final int MAX_COUNT_OF_OVERDUE_RESOURCES = 2;

        if (patron.overdueCheckoutsAt(toHold.getLibraryBranch()) >= MAX_COUNT_OF_OVERDUE_RESOURCES) {
            return left(new Rejection("cannot place on hold when there are overdue checkouts"));
        }
        return right(new Allowance());
    }
}

class RegularPatronMaximumNumberOfHoldsPolicy implements PlacingOnHoldPolicy {

    @Override
    public Either<Rejection, Allowance> canPlaceOnHold(Resource toHold, PatronResources patron) {

        final int MAX_NUMBER_OF_HOLDS = 5;

        if (patron.isRegular() && patron.numberOfHolds() >= MAX_NUMBER_OF_HOLDS) {
            return left(new Rejection("patron cannot hold more resources"));
        }
        return right(new Allowance());
    }
}

@Value
class Allowance { }

@Value
class Rejection {
    String reason;
}