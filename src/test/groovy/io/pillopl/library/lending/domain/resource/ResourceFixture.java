package io.pillopl.library.lending.domain.resource;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.ResourceOnHold;

import java.util.UUID;

import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch;
import static io.pillopl.library.lending.domain.resource.Resource.ResourceState.AVAILABLE;
import static io.pillopl.library.lending.domain.resource.Resource.ResourceState.COLLECTED;
import static io.pillopl.library.lending.domain.resource.Resource.ResourceState.ON_HOLD;
import static io.pillopl.library.lending.domain.resource.Resource.ResourceType.CIRCULATING;
import static io.pillopl.library.lending.domain.resource.Resource.ResourceType.RESTRICTED;

public class ResourceFixture {

    static ResourceId resourceId(UUID resourceId) {
        return new ResourceId(resourceId);
    }

    public static Resource resourceOnHold(ResourceId resourceId, LibraryBranchId libraryBranchId) {
        return new Resource(resourceId, libraryBranchId, CIRCULATING, ON_HOLD);
    }

    public static Resource circulatingResource() {
        return new Resource(anyResourceId(), anyBranch(), CIRCULATING, AVAILABLE);
    }

    public static Resource resourceOnHold() {
        return new Resource(anyResourceId(), anyBranch(), CIRCULATING, AVAILABLE);
    }

    public static ResourceOnHold onHold() {
        return new ResourceOnHold(anyResourceId(), anyBranch());
    }

    public static Resource circulatingResourceAt(LibraryBranchId libraryBranchId) {
        return new Resource(anyResourceId(), libraryBranchId, CIRCULATING, AVAILABLE);
    }

    static Resource collectedResource() {
        return new Resource(anyResourceId(), anyBranch(), CIRCULATING, COLLECTED);
    }

    public static Resource restrictedResource() {
        return new Resource(anyResourceId(), anyBranch(), RESTRICTED, AVAILABLE);
    }

    public static ResourceId anyResourceId() {
        return new ResourceId(UUID.randomUUID());
    }


}
