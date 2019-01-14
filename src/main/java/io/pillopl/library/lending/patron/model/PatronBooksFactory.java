package io.pillopl.library.lending.patron.model;

import static io.pillopl.library.lending.patron.model.PlacingOnHoldPolicy.allCurrentPolicies;

public class PatronBooksFactory {

    public PatronBooks recreateFrom(PatronInformation patronInformation, PatronHolds patronHolds, OverdueCheckouts overdueCheckouts) {
        return new PatronBooks(patronInformation, allCurrentPolicies(), overdueCheckouts, patronHolds);
    }

}
