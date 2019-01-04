package io.pillopl.library.lending.domain.patron;

import static io.pillopl.library.lending.domain.patron.PlacingOnHoldPolicy.allCurrentPolicies;

public class PatronBooksFactory {

    public PatronBooks recreateFrom(PatronInformation patronInformation, PatronHolds patronHolds, OverdueCheckouts overdueCheckouts) {
        return new PatronBooks(patronInformation, allCurrentPolicies(), overdueCheckouts, patronHolds);
    }

}
