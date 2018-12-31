package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.resource.Resource;
import io.pillopl.library.lending.domain.resource.ResourceId;
import lombok.AllArgsConstructor;
import lombok.Value;

//TODO add not null
@Value
@AllArgsConstructor
public class ResourceOnHold {

    ResourceId resourceId;
    LibraryBranchId libraryBranchId;

    ResourceOnHold(Resource resource) {
        this(resource.getResourceId(), resource.getLibraryBranch());
    }

}
