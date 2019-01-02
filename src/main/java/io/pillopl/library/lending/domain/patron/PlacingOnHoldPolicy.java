package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.book.AvailableBook;
import io.vavr.collection.List;
import io.vavr.control.Either;
import lombok.Value;

import java.util.function.BiFunction;

import static io.pillopl.library.lending.domain.patron.Reason.withReason;
import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

interface PlacingOnHoldPolicy extends BiFunction<AvailableBook, PatronBooks, Either<Rejection, Allowance>> {

    PlacingOnHoldPolicy onlyResearcherPatronsCanBookRestrictedBooksPolicy = (AvailableBook toHold, PatronBooks patron) -> {
        if (toHold.isRestricted() && patron.isRegular()) {
            return left(new Rejection(withReason("Regular patrons cannot hold restricted books")));
        }
        return right(new Allowance());
    };

    PlacingOnHoldPolicy overdueCheckoutsRejectionPolicy = (AvailableBook toHold, PatronBooks patron) -> {
        final int MAX_COUNT_OF_OVERDUE_RESOURCES = 2;

        if (patron.overdueCheckoutsAt(toHold.getLibraryBranch()) >= MAX_COUNT_OF_OVERDUE_RESOURCES) {
            return left(new Rejection(withReason("cannot place on hold when there are overdue checkouts")));
        }
        return right(new Allowance());
    };

    PlacingOnHoldPolicy regularPatronMaximumNumberOfHoldsPolicy = (AvailableBook toHold, PatronBooks patron) -> {
        final int MAX_NUMBER_OF_HOLDS = 5;

        if (patron.isRegular() && patron.numberOfHolds() >= MAX_NUMBER_OF_HOLDS) {
            return left(new Rejection(withReason("patron cannot hold more books")));
        }
        return right(new Allowance());
    };

    static List<PlacingOnHoldPolicy> allCurrentPolicies() {
        return List.of(
                onlyResearcherPatronsCanBookRestrictedBooksPolicy,
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