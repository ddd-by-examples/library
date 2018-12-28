package io.pillopl.library.domain;


import io.pillopl.library.domain.PatronResourcesEvents.ResourceCollected;
import io.pillopl.library.domain.PatronResourcesEvents.ResourceCollectingFailed;
import io.pillopl.library.domain.PatronResourcesEvents.ResourceHoldFailed;
import io.pillopl.library.domain.PatronResourcesEvents.ResourcePlacedOnHold;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static java.util.Collections.emptySet;

//TODO - open/close
//TODO - play with types
//TODO -
@AllArgsConstructor
@EqualsAndHashCode(of = "patron")
class PatronResources {

    private final PatronInformation patron;

    private final List<PlacingOnHoldPolicy> placingOnHoldPolicies;

    private OverdueCheckouts overdueCheckouts;

    private ResourcesOnHold resourcesOnHold;

    //TODO cannot hold held or collected - separate transaction
    Either<ResourceHoldFailed, ResourcePlacedOnHold> placeOnHold(Resource resource) {
        Option<Rejection> rejection = checkRejectionPolicy(resource);
        if (!rejection.isEmpty()) {
            return left(ResourceHoldFailed.now(
                    rejection.map(Rejection::getReason).getOrElse("couldn't hold resource"),
                    resource,
                    patron));
        }
        ResourceOnHold resourceOnHold = createResourceOnHold(resource);
        resourcesOnHold = resourcesOnHold.newActive(resourceOnHold);
        return right(resourceOnHold.toResourceHeld());
    }

    Either<ResourceCollectingFailed, ResourceCollected> collect(Resource resource) {
        ResourceOnHold resourceToCollect = createResourceOnHold(resource);
        if (resourcesOnHold.doesNotContain(resourceToCollect)) {
            return left(ResourceCollectingFailed.now(
                    "resource is not on hold by patron", resource, patron));
        }
        resourcesOnHold = resourcesOnHold.completed(resourceToCollect);
        return right(resourceToCollect.toResourceCollected());
    }

    private Option<Rejection> checkRejectionPolicy(Resource resource) {
        return placingOnHoldPolicies
                .map(policy -> policy.canPlaceOnHold(resource, this))
                .find(Either::isLeft)
                .map(Either::getLeft);
    }

    private ResourceOnHold createResourceOnHold(Resource resource) {
        return new ResourceOnHold(patron, resource);
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
class ResourcesOnHold {

    static final int REGULAR_PATRON_HOLDS_LIMIT = 5;

    Set<ResourceOnHold> resourcesOnHold;

    ResourcesOnHold newActive(ResourceOnHold resourceOnHold) {
        Set<ResourceOnHold> newResourcesOnHolds = new HashSet<>(resourcesOnHold);
        newResourcesOnHolds.add(resourceOnHold);
        return new ResourcesOnHold(newResourcesOnHolds);
    }

    ResourcesOnHold completed(ResourceOnHold resourceOnHold) {
        Set<ResourceOnHold> newResourcesOnHolds = new HashSet<>(resourcesOnHold);
        newResourcesOnHolds.remove(resourceOnHold);
        return new ResourcesOnHold(newResourcesOnHolds);
    }

    boolean doesNotContain(ResourceOnHold resourceOnHold) {
        return !resourcesOnHold.contains(resourceOnHold);
    }

    int count() {
        return resourcesOnHold.size();
    }
}

//TODO add not null
@Value
@AllArgsConstructor
class ResourceOnHold {

    PatronId patronId;
    ResourceId resourceId;
    LibraryBranchId libraryBranchId;

    ResourceOnHold(PatronInformation patron, Resource resource) {
        this(patron.getPatronId(), resource.getResourceId(), resource.getLibraryBranch());
    }

    ResourcePlacedOnHold toResourceHeld() {
        return new ResourcePlacedOnHold(
                Instant.now(),
                patronId.getPatronId(),
                resourceId.getResourceId(),
                libraryBranchId.getLibraryBranchId());
    }

    ResourceCollected toResourceCollected() {
        return new ResourceCollected(
                Instant.now(),
                patronId.getPatronId(),
                resourceId.getResourceId(),
                libraryBranchId.getLibraryBranchId());
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
}







