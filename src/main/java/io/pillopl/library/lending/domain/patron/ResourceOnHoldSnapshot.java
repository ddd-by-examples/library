package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.resource.ResourceId;
import lombok.Value;

@Value
public class ResourceOnHoldSnapshot {
    ResourceId resourceId;
    LibraryBranchId libraryBranchId;
}
