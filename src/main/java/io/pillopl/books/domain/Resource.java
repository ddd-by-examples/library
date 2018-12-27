package io.pillopl.books.domain;


import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.emptyList;

@AllArgsConstructor
class Patron {

    static final int REGULAR_PATRON_HOLDS_LIMIT = 5;
    static final int MAX_COUNT_OF_OVERDUE_RESOURCES = 2;

    enum PatronType {RESEARCHER, REGULAR}

    private final OverdueResources overdueResources;

    private final PatronType type;

    private int numberOfHolds;

    void hold(Resource resource) {

        if (overdueResources.countAt(resource.branch()) >= MAX_COUNT_OF_OVERDUE_RESOURCES) {
            throw new ResourceHoldRequestFailed("Cannot hold resource, patron cannot hold in libraryBranch");
        }

        if (this.isRegular() && numberOfHolds >= REGULAR_PATRON_HOLDS_LIMIT) {
            throw new ResourceHoldRequestFailed("Cannot hold resource, patron cannot hold more resources");
        }

        if(this.isRegular() && resource.isRestricted()) {
            throw new ResourceHoldRequestFailed("Regular patrons cannot hold restricted resources");
        }

        resource.hold();
        numberOfHolds++;
    }

    private boolean isRegular() {
        return type.equals(PatronType.REGULAR);
    }

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

    LibraryBranchId branch() {
        return libraryBranch;
    }

    enum ResourceState {AVAILABLE, ON_HOLD, COLLECTED}

    enum ResourceType {RESTRICTED, NORMAL}

    private final LibraryBranchId libraryBranch;
    private final ResourceType type;
    private ResourceState state;

    void hold() {
        if (!isAvailable()) {
            throw new ResourceHoldRequestFailed("Cannot hold resource, resource is currently not available");
        }
        this.state = ResourceState.ON_HOLD;
    }

    boolean isRestricted() {
        return type.equals(ResourceType.RESTRICTED);
    }

    private boolean isAvailable() {
        return state.equals(ResourceState.AVAILABLE);
    }

    void collectBy(Patron patron) {

    }

    void returnedBy(Patron patron) {

    }

    boolean isHeld() {
        return state.equals(ResourceState.ON_HOLD);
    }
}


@Value
class ResourceId {

    String resourceId;

}




