package io.pillopl.books.domain;


import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.pillopl.books.domain.Resource.ResourceState.COLLECTED;
import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.emptyList;

@AllArgsConstructor
@EqualsAndHashCode(of = "patronId")
class Patron {

    static final int REGULAR_PATRON_HOLDS_LIMIT = 5;
    static final int MAX_COUNT_OF_OVERDUE_RESOURCES = 2;

    enum PatronType {RESEARCHER, REGULAR}

    @Getter
    private final PatronId patronId;

    private final OverdueResources overdueResources;

    private final PatronType type;

    private int numberOfHolds;

    Try<Void> hold(Resource resource) {
        return Try.run(() -> {
            if (overdueResources.countAt(resource.getLibraryBranch()) >= MAX_COUNT_OF_OVERDUE_RESOURCES) {
                throw new ResourceHoldRequestFailed("Cannot hold resource, patron cannot hold in libraryBranch");
            }

            if (this.isRegular() && numberOfHolds >= REGULAR_PATRON_HOLDS_LIMIT) {
                throw new ResourceHoldRequestFailed("Cannot hold resource, patron cannot hold more resources");
            }

            if (this.isRegular() && resource.isRestricted()) {
                throw new ResourceHoldRequestFailed("Regular patrons cannot hold restricted resources");
            }

            resource.holdBy(this);
            numberOfHolds++;
        });
    }

    private boolean isRegular() {
        return type.equals(PatronType.REGULAR);
    }

}

@Value
class PatronResourcesOnHold {

    static PatronResourcesOnHold of(List<ResourceOnHold> resourcesOnHold) {
        return new PatronResourcesOnHold(resourcesOnHold);
    }

    List<ResourceOnHold> resourcesOnHold;

    PatronResourcesOnHold(List<ResourceOnHold> resourcesOnHolds) {
        this.resourcesOnHold = resourcesOnHolds;
    }

}

@Value
class ResourceOnHold {
    PatronId patronId;
    ResourceId resourceId;
    LibraryBranchId libraryBranchId;
}

@Value
class PatronId {

    String patronId;

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
        return new OverdueResources(EMPTY_MAP);
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

    final String libraryBranchId;
}


@AllArgsConstructor
class Resource {

    enum ResourceState {AVAILABLE, ON_HOLD, COLLECTED}

    enum ResourceType {RESTRICTED, CIRCULATING}

    @Getter
    private final LibraryBranchId libraryBranch;

    private final ResourceType type;

    private ResourceState state;

    private PatronId heldBy;

    void holdBy(Patron patron) {
        if (!isAvailable()) {
            throw new ResourceHoldRequestFailed("Cannot hold resource, resource is currently not available");
        }
        this.heldBy = patron.getPatronId();
        this.state = ResourceState.ON_HOLD;
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

    Try<Void> collectBy(PatronId collectingPatron) {
        return Try.run(() ->{
            if(!isHeld()) {
                throw new ResourceCollectingFailed("resource is not on hold");
            }
            if(!collectingPatron.equals(this.heldBy)) {
                throw new ResourceCollectingFailed("resource should be collected by the patron who put it on hold");
            }
            this.heldBy = null;
            this.state = COLLECTED;
        });
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

    String resourceId;

}




