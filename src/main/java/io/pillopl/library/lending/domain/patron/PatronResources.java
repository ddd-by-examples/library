package io.pillopl.library.lending.domain.patron;


import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvent.ResourceCollected;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvent.ResourceCollectingFailed;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvent.ResourceHoldFailed;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvent.ResourcePlacedOnHold;
import io.pillopl.library.lending.domain.resource.Resource;
import io.pillopl.library.lending.domain.resource.ResourceId;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.pillopl.library.lending.domain.patron.Reason.withReason;
import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static java.util.Collections.emptySet;

//TODO - open/close
//TODO - play with types
@AllArgsConstructor
@EqualsAndHashCode(of = "patron")
public class PatronResources {

    private final PatronInformation patron;

    private final List<PlacingOnHoldPolicy> placingOnHoldPolicies;

    private final OverdueCheckouts overdueCheckouts;

    private final ResourcesOnHold resourcesOnHold;

    public Either<ResourceHoldFailed, ResourcePlacedOnHold> placeOnHold(Resource resource) {
        if(!resource.isAvailable()) {
            return left(ResourceHoldFailed.now(new Reason("resource is not available"), resource.getResourceId(), resource.getLibraryBranch(), patron));
        }
        Option<Rejection> rejection = tryPlacingOnHold(resource);
        if (!rejection.isEmpty()) {
            return left(ResourceHoldFailed.now(rejection.get().getReason(), resource.getResourceId(), resource.getLibraryBranch(), patron));
        }
        return right(ResourcePlacedOnHold.now(resource.getResourceId(), resource.getLibraryBranch(), patron));
    }

    public Either<ResourceCollectingFailed, ResourceCollected> collect(Resource resource) {
        ResourceOnHold resourceToCollect = new ResourceOnHold(resource);
        if (resourcesOnHold.doesNotContain(resourceToCollect)) {
            return left(ResourceCollectingFailed.now(withReason("resource is not on hold by patron"), resource.getResourceId(), resource.getLibraryBranch(), patron));
        }
        return right(ResourceCollected.now(resourceToCollect.getResourceId(), resourceToCollect.getLibraryBranchId(), patron.getPatronId()));
    }

    private Option<Rejection> tryPlacingOnHold(Resource resource) {
        return placingOnHoldPolicies
                .toStream()
                .map(policy -> policy.canPlaceOnHold(resource, this))
                .find(Either::isLeft)
                .map(Either::getLeft);
    }

    boolean isRegular() {
        return patron.isRegular();
    }

    int overdueCheckoutsAt(LibraryBranchId libraryBranch) {
        return overdueCheckouts.countAt(libraryBranch);
    }

    int numberOfHolds() {
        return resourcesOnHold.count();
    }

}

@Value
//TODO add not null
class OverdueCheckouts {

    Map<LibraryBranchId, Set<ResourceId>> overdueCheckouts;

    static OverdueCheckouts noOverdueCheckouts() {
        return new OverdueCheckouts(new HashMap<>());
    }

    int countAt(LibraryBranchId libraryBranchId) {
        return overdueCheckouts.getOrDefault(libraryBranchId, emptySet()).size();
    }

}







