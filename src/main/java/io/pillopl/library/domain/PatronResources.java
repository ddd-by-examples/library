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

    private final OverdueCheckouts overdueCheckouts;

    private ResourcesOnHold resourcesOnHold;

    //TODO cannot hold held or collected - separate transaction
    Either<ResourceHoldFailed, ResourcePlacedOnHold> placeOnHold(Resource resource) {
        Option<Rejection> rejection = tryPlacingOnHold(resource);
        if (!rejection.isEmpty()) {
            return left(ResourceHoldFailed.now(
                    rejection.map(Rejection::getReason).getOrElse("couldn't hold resource"),
                    resource,
                    patron));
        }
        ResourcePlacedOnHold resourcePlacedOnHold = resourcesOnHold.hold(resource, patron);
        return right(resourcePlacedOnHold);
    }

    Either<ResourceCollectingFailed, ResourceCollected> collect(Resource resource) {
        ResourceOnHold resourceToCollect = new ResourceOnHold(patron, resource);
        if (resourcesOnHold.doesNotContain(resourceToCollect)) {
            return left(ResourceCollectingFailed.now("resource is not on hold by patron", resource, patron));
        }
        ResourceCollected resourceCollected = resourcesOnHold.completed(resourceToCollect);
        return right(resourceCollected);
    }

    private Option<Rejection> tryPlacingOnHold(Resource resource) {
        return placingOnHoldPolicies
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
class ResourcesOnHold {

    static final int REGULAR_PATRON_HOLDS_LIMIT = 5;

    Set<ResourceOnHold> resourcesOnHold;

    ResourcePlacedOnHold hold(Resource resourceToHold, PatronInformation patronInformation) {
        ResourceOnHold resourceOnHold = new ResourceOnHold(patronInformation, resourceToHold);
        resourcesOnHold.add(resourceOnHold);
        return resourceOnHold.toResourceHeld();
    }

    ResourceCollected completed(ResourceOnHold resourceToCollect) {
        resourcesOnHold.remove(resourceToCollect);
        return resourceToCollect.toResourceCollected();
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







