package io.pillopl.library.lending.domain.resource;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static io.pillopl.library.lending.domain.resource.Resource.ResourceState.COLLECTED;
import static io.pillopl.library.lending.domain.resource.Resource.ResourceState.ON_HOLD;

@AllArgsConstructor
public class Resource {

    enum ResourceState {AVAILABLE, ON_HOLD, COLLECTED}

    enum ResourceType {RESTRICTED, CIRCULATING}

    @Getter
    private final ResourceId resourceId;

    @Getter
    private final LibraryBranchId libraryBranch;

    private final ResourceType type;

    private ResourceState state;

    public boolean isRestricted() {
        return type.equals(ResourceType.RESTRICTED);
    }

    public boolean isAvailable() {
        return state.equals(ResourceState.AVAILABLE);
    }

    //TODO events upper
    public void handle(PatronResourcesEvent.ResourceReturned event) {
        this.state = ResourceState.AVAILABLE;
    }

    public void handle(PatronResourcesEvent.ResourcePlacedOnHold event) {
        state = ON_HOLD;
    }

    public void handle(PatronResourcesEvent.ResourceCollected event) {
        state = COLLECTED;
    }
}

