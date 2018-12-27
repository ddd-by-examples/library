package io.pillopl.books.domain;


import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.EMPTY_MAP;
import static java.util.Collections.emptyList;


@AllArgsConstructor
class Resource {

    enum ResourceState {AVAILABLE, ON_HOLD, COLLECTED}

    enum ResourceType {RESTRICTED, NORMAL}

    private final LibraryBranchId libraryBranch;
    private final ResourceType type;
    private ResourceState state;

    void holdBy(Patron patron) {
        //patron knows more here than resource itself. Probably patron should drive the process, not resource
        if (!isAvailable()) {
            throw new ResourceHoldRequestFailed("Cannot hold resource, resource is currently " + state);
        }
        if(!patron.canHoldResourceAt(libraryBranch)) {
            throw new ResourceHoldRequestFailed("Cannot hold resource, patron cannot hold in libraryBranch");
        }

        if(patron.isRegular() && this.isRestricted()) {
            throw new ResourceHoldRequestFailed("Regular patrons cannot hold restricted resources");
        }
        this.state = ResourceState.ON_HOLD;
        patron.heldResource();
    }

    private boolean isRestricted() {
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

class ResourceHoldRequestFailed extends RuntimeException {
    ResourceHoldRequestFailed(String msg) {
        super(msg);
    }
}

@AllArgsConstructor
class Patron {

    static final int REGULAR_PATRON_HOLDS_LIMIT = 5;
    static final int MAX_COUNT_OF_OVERDUE_RESOURCES = 2;

    enum PatronType {RESEARCHER, REGULAR}

    private final OverdueResources overdueResources;

    private final PatronType type;

    private int numberOfHolds;

    static Patron regular() {
        return new Patron(OverdueResources.noOverdueResources(), PatronType.REGULAR, 0);
    }

    static Patron researcher() {
        return new Patron(OverdueResources.noOverdueResources(), PatronType.RESEARCHER, 0);
    }

    static Patron regularWithHolds(int numberOfHolds) {
        return new Patron(OverdueResources.noOverdueResources(), PatronType.REGULAR, numberOfHolds);
    }

    static Patron researcherWithHolds(int numberOfHolds) {
        return new Patron(OverdueResources.noOverdueResources(), PatronType.RESEARCHER, numberOfHolds);
    }

    static Patron regularWithOverdueResource(OverdueResources overdueResources) {
        return new Patron(overdueResources, PatronType.REGULAR, 0);
    }

    static Patron reasearcherWithOverdueResource(OverdueResources overdueResources) {
        return new Patron(overdueResources, PatronType.RESEARCHER, 0);
    }

    boolean canHoldResourceAt(LibraryBranchId libraryBranch) {
        if (this.isRegular() && numberOfHolds >= REGULAR_PATRON_HOLDS_LIMIT) {
            return false;
        }

        if (overdueResources.countAt(libraryBranch) >= MAX_COUNT_OF_OVERDUE_RESOURCES) {
            return false;
        }
        return true;
    }

    void heldResource() {
        numberOfHolds++;
    }

    boolean isRegular() {
        return type.equals(PatronType.REGULAR);
    }


}


@Value
class LibraryBranchId {

    final String libraryBranchId;
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



