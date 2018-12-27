package io.pillopl.books.domain;

import static io.pillopl.books.domain.LibraryBranchFixture.anyBranch;
import static io.pillopl.books.domain.Resource.ResourceState.AVAILABLE;
import static io.pillopl.books.domain.Resource.ResourceState.COLLECTED;
import static io.pillopl.books.domain.Resource.ResourceState.ON_HOLD;
import static io.pillopl.books.domain.Resource.ResourceType.CIRCULATING;
import static io.pillopl.books.domain.Resource.ResourceType.RESTRICTED;

class ResourceFixture {

    static ResourceId resourceId(String resourceId) {
        return new ResourceId(resourceId);
    }

    static Resource resourceOnHold() {
        return new Resource(anyBranch(), CIRCULATING, ON_HOLD);
    }

    static Resource circulatingResource() {
        return new Resource(anyBranch(), CIRCULATING, AVAILABLE);
    }

    static Resource collectedResource() {
        return new Resource(anyBranch(), CIRCULATING, COLLECTED);
    }

    static Resource restrictedResource() {
        return new Resource(anyBranch(), RESTRICTED, AVAILABLE);
    }

}
