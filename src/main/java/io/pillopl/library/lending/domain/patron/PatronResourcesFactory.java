package io.pillopl.library.lending.domain.patron;

import static java.util.stream.Collectors.toSet;

public class PatronResourcesFactory {

    public PatronResources recreateFrom(PatronResourcesSnapshot snapshot) {
        ResourcesOnHold resourcesOnHold =
                new ResourcesOnHold(
                        snapshot
                        .getResourcesOnHold()
                        .stream()
                        .map(resourceOnHoldSnapshot -> new ResourceOnHold(resourceOnHoldSnapshot.getResourceId(), resourceOnHoldSnapshot.getLibraryBranchId()))
                        .collect(toSet()));
        //TODO overdue checkouts
        return new PatronResources(snapshot.getPatronInformation(), PlacingOnHoldPolicy.allCurrentPolicies(), OverdueCheckouts.noOverdueCheckouts(), resourcesOnHold);
    }
}
