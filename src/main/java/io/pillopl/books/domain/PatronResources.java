package io.pillopl.books.domain;


import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.*;

import static io.pillopl.books.domain.Resource.ResourceState.COLLECTED;
import static java.util.Collections.emptyList;

@AllArgsConstructor
@EqualsAndHashCode(of = "patronId")
class PatronResources {

    static final int REGULAR_PATRON_HOLDS_LIMIT = 5;
    static final int MAX_COUNT_OF_OVERDUE_RESOURCES = 2;

    enum PatronType {RESEARCHER, REGULAR}

    @Getter
    private final PatronId patronId;

    private final PatronType type;

    private OverdueResources overdueResources;

    private ResourcesOnHold resourcesOnHold;

    Try<Void> hold(Resource resource) {
        return Try.run(() -> {
            if (this.isRegular() && resourcesOnHold.count() >= REGULAR_PATRON_HOLDS_LIMIT) {
                throw new ResourceHoldRequestFailed("Cannot hold resource, patron cannot hold more resources");
            }

            if (this.isRegular() && resource.isRestricted()) {
                throw new ResourceHoldRequestFailed("Regular patrons cannot hold restricted resources");
            }

            if (overdueResources.countAt(resource.getLibraryBranch()) >= MAX_COUNT_OF_OVERDUE_RESOURCES) {
                throw new ResourceHoldRequestFailed("Cannot hold resource, patron cannot hold in libraryBranch");
            }

            resource.hold();
            resourcesOnHold = resourcesOnHold.with(new ResourceOnHold(patronId, resource.getResourceId(), resource.getLibraryBranch()));
        });
    }

    Try<Void> collect(Resource resource) {
        return Try.run(() ->{
            ResourceOnHold resourceToCollect = new ResourceOnHold(patronId, resource.getResourceId(), resource.getLibraryBranch());

            if(!resourcesOnHold.contains(resourceToCollect)) {
                throw new ResourceCollectingFailed("resource is not on hold by patron");

            }
            resource.collect();
            resourcesOnHold = resourcesOnHold.without(resourceToCollect);
        });
    }


    private boolean isRegular() {
        return type.equals(PatronType.REGULAR);
    }

}

@Value
class ResourceOnHold {
    PatronId patronId;
    ResourceId resourceId;
    LibraryBranchId libraryBranchId;
}

@Value
class ResourcesOnHold {

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

    int count() {
        return resourcesOnHold.size();
    }

    boolean contains(ResourceOnHold resourceOnHold) {
        return resourcesOnHold.contains(resourceOnHold);
    }

}

@Value
class PatronId {

    UUID patronId;

}

class ResourceHoldRequestFailed extends RuntimeException {
    ResourceHoldRequestFailed(String msg) {
        super(msg);
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


@Value
class LibraryBranchId {

    final UUID libraryBranchId;
}


@AllArgsConstructor
class Resource {


    enum ResourceState {AVAILABLE, ON_HOLD, COLLECTED}

    enum ResourceType {RESTRICTED, CIRCULATING}

    @Getter
    private final ResourceId resourceId;

    @Getter
    private final LibraryBranchId libraryBranch;

    private final ResourceType type;

    private ResourceState state;

    void hold() {
        if (!isAvailable()) {
            throw new ResourceHoldRequestFailed("Cannot hold resource, resource is currently not available");
        }
        this.state = ResourceState.ON_HOLD;
    }

    void collect() {
        state = COLLECTED;
    }

    boolean isRestricted() {
        return type.equals(ResourceType.RESTRICTED);
    }

    boolean isCollected() {
        return state.equals(COLLECTED);
    }

    boolean isAvailable() {
        return state.equals(ResourceState.AVAILABLE);
    }

    boolean isHeld() {
        return state.equals(ResourceState.ON_HOLD);
    }

    void returned() {
        this.state = ResourceState.AVAILABLE;
    }


}

class ResourceCollectingFailed extends RuntimeException {
    ResourceCollectingFailed(String msg) {
        super(msg);
    }
}


@Value
class ResourceId {

    UUID resourceId;

}




