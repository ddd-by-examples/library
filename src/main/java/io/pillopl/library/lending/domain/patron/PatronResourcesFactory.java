package io.pillopl.library.lending.domain.patron;

public class PatronResourcesFactory {


    public PatronResources recreateFrom(PatronInformation patronInformation,
                                        ResourcesOnHold resourcesOnHold) {
        //overduecheckouts
        return new PatronResources(
                patronInformation,
                PlacingOnHoldPolicy.allCurrentPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                resourcesOnHold);
    }

}
