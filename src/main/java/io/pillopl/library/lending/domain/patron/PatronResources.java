package io.pillopl.library.lending.domain.patron;


import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvents.ResourceCollected;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvents.ResourceCollectingFailed;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvents.ResourceHoldFailed;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvents.ResourcePlacedOnHold;
import io.pillopl.library.lending.domain.resource.Resource;
import io.pillopl.library.lending.domain.resource.ResourceId;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    private ResourcesOnHold resourcesOnHold;

    public Either<ResourceHoldFailed, ResourcePlacedOnHold> placeOnHold(Resource resource) {
        Option<Rejection> rejection = tryPlacingOnHold(resource);
        if (!rejection.isEmpty()) {
            return left(
                    ResourceHoldFailed.now(rejection.get().getReason(), resource.getResourceId(), resource.getLibraryBranch(), patron));
        }
        ResourcePlacedOnHold resourcePlacedOnHold = resourcesOnHold.hold(resource, patron);
        return right(resourcePlacedOnHold);
    }

    public Either<ResourceCollectingFailed, ResourceCollected> collect(Resource resource) {
        ResourceOnHold resourceToCollect = new ResourceOnHold(resource);
        if (resourcesOnHold.doesNotContain(resourceToCollect)) {
            return left(ResourceCollectingFailed.now("resource is not on hold by patron", resource.getResourceId(), resource.getLibraryBranch(), patron));
        }
        ResourceCollected resourceCollected = resourcesOnHold.complete(resourceToCollect, patron);
        return right(resourceCollected);
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

    public PatronResourcesSnapshot toSnapshot() {
        return new PatronResourcesSnapshot(patron, resourcesOnHold.toSnapshot(), overdueCheckouts.toSnapshot());
    }
}

@AllArgsConstructor
//TODO add not null
class ResourcesOnHold {

    Set<ResourceOnHold> resourcesOnHold;

    ResourcePlacedOnHold hold(Resource resourceToHold, PatronInformation patronInformation) {
        ResourceOnHold resourceOnHold = new ResourceOnHold(resourceToHold);
        resourcesOnHold.add(resourceOnHold);
        return ResourcePlacedOnHold.now(resourceOnHold.getResourceId(), resourceOnHold.getLibraryBranchId(), patronInformation);
    }

    ResourceCollected complete(ResourceOnHold resourceToCollect, PatronInformation patronInformation) {
        resourcesOnHold.remove(resourceToCollect);
        return ResourceCollected.now(resourceToCollect.getResourceId(), resourceToCollect.getLibraryBranchId(), patronInformation.getPatronId());
    }

    boolean doesNotContain(ResourceOnHold resourceOnHold) {
        return !resourcesOnHold.contains(resourceOnHold);
    }

    int count() {
        return resourcesOnHold.size();
    }

    Set<ResourceOnHoldSnapshot> toSnapshot() {
        return resourcesOnHold
                .stream()
                .map(ResourceOnHold::toSnapshot)
                .collect(Collectors.toSet());
    }
}

//TODO add not null
@Value
@AllArgsConstructor
class ResourceOnHold {

    ResourceId resourceId;
    LibraryBranchId libraryBranchId;

    ResourceOnHold(Resource resource) {
        this(resource.getResourceId(), resource.getLibraryBranch());
    }

    ResourceOnHoldSnapshot toSnapshot() {
        return new ResourceOnHoldSnapshot(resourceId, libraryBranchId);
    }

}

@Value
//TODO add not null
class OverdueCheckouts {

    Map<LibraryBranchId, Set<ResourceId>> overdueCheckouts;

    static OverdueCheckouts noOverdueCheckouts() {
        return new OverdueCheckouts(new HashMap<>());
    }

    static OverdueCheckouts atBranch(LibraryBranchId libraryBranch, Set<ResourceId> resourcesId) {
        Map<LibraryBranchId, Set<ResourceId>> overdueResources = new HashMap<>();
        overdueResources.put(libraryBranch, resourcesId);
        return new OverdueCheckouts(overdueResources);
    }

    int countAt(LibraryBranchId libraryBranchId) {
        return overdueCheckouts.getOrDefault(libraryBranchId, emptySet()).size();
    }

    Map<LibraryBranchId, Set<ResourceId>> toSnapshot() {
        return Collections.unmodifiableMap(overdueCheckouts);
    }
}







