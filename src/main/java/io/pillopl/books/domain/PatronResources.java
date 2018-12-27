package io.pillopl.books.domain;


import io.pillopl.books.domain.PatronResourcesEvents.ResourceCollected;
import io.pillopl.books.domain.PatronResourcesEvents.ResourceCollectingFailed;
import io.pillopl.books.domain.PatronResourcesEvents.ResourceHeld;
import io.pillopl.books.domain.PatronResourcesEvents.ResourceHoldRequestFailed;
import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.Instant;
import java.util.*;

import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static java.util.Collections.emptyList;

@AllArgsConstructor
@EqualsAndHashCode(of = "patron")
class PatronResources {

    static final int MAX_COUNT_OF_OVERDUE_RESOURCES = 2;

    private final PatronInformation patron;

    private OverdueResources overdueResources;

    private ResourcesOnHold resourcesOnHold;

    //TODO cannot hold held or collected
    Either<ResourceHoldRequestFailed, ResourceHeld> hold(Resource resource) {
        if (regularPatronIsTryingToExceedMaxNumberOfHolds()) {
            return left(resourceHoldRequestFailed(resource, "Cannot hold resource, patron cannot hold more resources"));
        }

        if (regularPatronIsTryingToRequestRestrictedResource(resource)) {
            return left(resourceHoldRequestFailed(resource, "Regular patrons cannot hold restricted resources"));
        }

        if (patronHasMaximumNumberOfOverdueCheckouts(resource)) {
            return left(resourceHoldRequestFailed(resource, "Cannot hold resource, patron cannot hold in libraryBranch"));
        }

        ResourceOnHold resourceOnHold = new ResourceOnHold(patron.getPatronId(), resource.getResourceId(), resource.getLibraryBranch());
        resourcesOnHold = resourcesOnHold.with(resourceOnHold);
        return right(resourceOnHold.toResourceHeld());
    }

    Either<ResourceCollectingFailed, ResourceCollected> collect(Resource resource) {

        ResourceOnHold resourceToCollect = new ResourceOnHold(patron.getPatronId(), resource.getResourceId(), resource.getLibraryBranch());

        if (!resourcesOnHold.contains(resourceToCollect)) {
            return left(resourceCollectingFailed(resource, "resource is not on hold by patron"));
        }
        resourcesOnHold = resourcesOnHold.without(resourceToCollect);
        return right(resourceToCollect.toResourceCollected());
    }

    private boolean regularPatronIsTryingToExceedMaxNumberOfHolds() {
        return patron.isRegular() && resourcesOnHold.cannotHoldMore();
    }


    private boolean patronHasMaximumNumberOfOverdueCheckouts(Resource resource) {
        return overdueResources.countAt(resource.getLibraryBranch()) >= MAX_COUNT_OF_OVERDUE_RESOURCES;
    }

    private boolean regularPatronIsTryingToRequestRestrictedResource(Resource resource) {
        return patron.isRegular() && resource.isRestricted();
    }

    private ResourceHoldRequestFailed resourceHoldRequestFailed(Resource resource, String reason) {
        return new ResourceHoldRequestFailed(reason,
                Instant.now(),
                patron.getPatronId().getPatronId(),
                resource.getResourceId().getResourceId(),
                resource.getLibraryBranch().getLibraryBranchId());
    }

    private ResourceCollectingFailed resourceCollectingFailed(Resource resource, String reason) {
        return new ResourceCollectingFailed(reason,
                Instant.now(),
                patron.getPatronId().getPatronId(),
                resource.getResourceId().getResourceId(),
                resource.getLibraryBranch().getLibraryBranchId());
    }


}

@Value
class ResourcesOnHold {

    static final int REGULAR_PATRON_HOLDS_LIMIT = 5;

    Set<ResourceOnHold> resourcesOnHold;

    ResourcesOnHold with(ResourceOnHold resourceOnHold) {
        Set<ResourceOnHold> newResourcesOnHolds = new HashSet<>(resourcesOnHold);
        newResourcesOnHolds.add(resourceOnHold);
        return new ResourcesOnHold(newResourcesOnHolds);
    }

    ResourcesOnHold without(ResourceOnHold resourceOnHold) {
        Set<ResourceOnHold> newResourcesOnHolds = new HashSet<>(resourcesOnHold);
        newResourcesOnHolds.remove(resourceOnHold);
        return new ResourcesOnHold(newResourcesOnHolds);
    }

    boolean contains(ResourceOnHold resourceOnHold) {
        return resourcesOnHold.contains(resourceOnHold);
    }

    boolean cannotHoldMore() {
        return resourcesOnHold.size() >= REGULAR_PATRON_HOLDS_LIMIT;
    }
}

@Value
class ResourceOnHold {
    PatronId patronId;
    ResourceId resourceId;
    LibraryBranchId libraryBranchId;

    ResourceHeld toResourceHeld() {
        return new ResourceHeld(Instant.now(), patronId.getPatronId(), resourceId.getResourceId(), libraryBranchId.getLibraryBranchId());
    }

    ResourceCollected toResourceCollected() {
        return new ResourceCollected(Instant.now(), patronId.getPatronId(), resourceId.getResourceId(), libraryBranchId.getLibraryBranchId());
    }
}

@Value
class OverdueResources {

    Map<LibraryBranchId, List<ResourceId>> overdueResources;

    static OverdueResources noOverdueResources() {
        return new OverdueResources(new HashMap<>());
    }

    static OverdueResources atBranch(LibraryBranchId libraryBranch, List<ResourceId> resourcesId) {
        Map<LibraryBranchId, List<ResourceId>> overdueResources = new HashMap<>();
        overdueResources.put(libraryBranch, resourcesId);
        return new OverdueResources(overdueResources);
    }

    int countAt(LibraryBranchId libraryBranchId) {
        return overdueResources.getOrDefault(libraryBranchId, emptyList()).size();
    }
}







