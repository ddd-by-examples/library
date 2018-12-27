package io.pillopl.books.domain;

import java.util.UUID;

import static io.pillopl.books.domain.LibraryBranchFixture.anyBranch;
import static io.pillopl.books.domain.Resource.ResourceState.AVAILABLE;
import static io.pillopl.books.domain.Resource.ResourceState.COLLECTED;
import static io.pillopl.books.domain.Resource.ResourceState.ON_HOLD;
import static io.pillopl.books.domain.Resource.ResourceType.CIRCULATING;
import static io.pillopl.books.domain.Resource.ResourceType.RESTRICTED;

class ResourceFixture {

    static ResourceId resourceId(UUID resourceId) {
        return new ResourceId(resourceId);
    }

    static Resource resourceOnHold() {
        return new Resource(anyResourceId(), anyBranch(), CIRCULATING, ON_HOLD);
    }

    static Resource circulatingResource() {
        return new Resource(anyResourceId(), anyBranch(), CIRCULATING, AVAILABLE);
    }

    static Resource circulatingResource(LibraryBranchId libraryBranchId) {
        return new Resource(anyResourceId(), libraryBranchId, CIRCULATING, AVAILABLE);
    }

    static Resource collectedResource() {
        return new Resource(anyResourceId(), anyBranch(), CIRCULATING, COLLECTED);
    }

    static Resource restrictedResource() {
        return new Resource(anyResourceId(), anyBranch(), RESTRICTED, AVAILABLE);
    }

    static ResourceId anyResourceId() {
        return new ResourceId(UUID.randomUUID());
    }


}
