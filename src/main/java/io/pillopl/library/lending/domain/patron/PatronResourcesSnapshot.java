package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.resource.ResourceId;
import lombok.Value;

import java.util.Map;
import java.util.Set;

@Value
public class PatronResourcesSnapshot {
    PatronInformation patronInformation;
    Set<ResourceOnHoldSnapshot> resourcesOnHold;
    Map<LibraryBranchId, Set<ResourceId>> overdueCheckouts;


}
