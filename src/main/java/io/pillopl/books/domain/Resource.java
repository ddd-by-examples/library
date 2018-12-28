package io.pillopl.books.domain;

import io.pillopl.books.domain.PatronResourcesEvents.ResourceCollected;
import io.pillopl.books.domain.PatronResourcesEvents.ResourcePlacedOnHold;
import io.pillopl.books.domain.PatronResourcesEvents.ResourceReturned;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.UUID;

import static io.pillopl.books.domain.Resource.ResourceState.COLLECTED;
import static io.pillopl.books.domain.Resource.ResourceState.ON_HOLD;

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

    boolean isCollected() {
        return state.equals(COLLECTED);
    }

    boolean isAvailable() {
        return state.equals(ResourceState.AVAILABLE);
    }

    boolean isHeld() {
        return state.equals(ON_HOLD);
    }

    void handle(ResourceReturned event) {
        this.state = ResourceState.AVAILABLE;
    }

    void handle(ResourcePlacedOnHold event) {
        state = ON_HOLD;
    }

    void handle(ResourceCollected event) {
        state = COLLECTED;
    }
}

@Value
//TODO add not null
class ResourceId {

    UUID resourceId;
}
