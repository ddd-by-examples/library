package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.book.model.AvailableBook;
import io.vavr.Function3;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.NonNull;
import lombok.Value;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

interface PlacingOnHoldPolicy extends Function3<AvailableBook, Patron, HoldDuration, Either<Rejection, Allowance>> {

    PlacingOnHoldPolicy onlyResearcherPatronsCanHoldRestrictedBooksPolicy = (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (toHold.isRestricted() && patron.isRegular()) {
            return left(Rejection.withReason("Regular patrons cannot hold restricted books"));
        }
        return right(new Allowance());
    };

    PlacingOnHoldPolicy overdueCheckoutsRejectionPolicy = (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (patron.overdueCheckoutsAt(toHold.getLibraryBranch()) >= OverdueCheckouts.MAX_COUNT_OF_OVERDUE_RESOURCES) {
            return left(Rejection.withReason("cannot place on hold when there are overdue checkouts"));
        }
        return right(new Allowance());
    };

    PlacingOnHoldPolicy regularPatronMaximumNumberOfHoldsPolicy = (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (patron.isRegular() && patron.numberOfHolds() >= PatronHolds.MAX_NUMBER_OF_HOLDS) {
            return left(Rejection.withReason("patron cannot hold more books"));
        }
        return right(new Allowance());
    };

    PlacingOnHoldPolicy onlyResearcherPatronsCanPlaceOpenEndedHolds = (AvailableBook toHold, Patron patron, HoldDuration holdDuration) -> {
        if (patron.isRegular() && holdDuration.isOpenEnded()) {
            return left(Rejection.withReason("regular patron cannot place open ended holds"));
        }
        return right(new Allowance());
    };

    static List<PlacingOnHoldPolicy> allCurrentPolicies() {
        return List.of(
                onlyResearcherPatronsCanHoldRestrictedBooksPolicy,
                overdueCheckoutsRejectionPolicy,
                regularPatronMaximumNumberOfHoldsPolicy,
                onlyResearcherPatronsCanPlaceOpenEndedHolds);
    }

}

@Value
class Allowance { }

@Value
class Rejection {

    @Value
    static class Reason {
        @NonNull
        String reason;
    }

    @NonNull
    Reason reason;

    static Rejection withReason(String reason) {
        return new Rejection(new Reason(reason));
    }
}

