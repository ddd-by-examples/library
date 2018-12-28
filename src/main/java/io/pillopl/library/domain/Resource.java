package io.pillopl.library.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.UUID;

import static io.pillopl.library.domain.Resource.ResourceState.COLLECTED;
import static io.pillopl.library.domain.Resource.ResourceState.ON_HOLD;

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

    boolean isRestricted() {
        return type.equals(ResourceType.RESTRICTED);
    }

    boolean isAvailable() {
        return state.equals(ResourceState.AVAILABLE);
    }

    void handle(PatronResourcesEvents.ResourceReturned event) {
        this.state = ResourceState.AVAILABLE;
    }

    void handle(PatronResourcesEvents.ResourcePlacedOnHold event) {
        state = ON_HOLD;
    }

    void handle(PatronResourcesEvents.ResourceCollected event) {
        state = COLLECTED;
    }
}

@Value
//TODO add not null
class ResourceId {

    UUID resourceId;
}
