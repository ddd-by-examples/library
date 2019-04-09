package io.pillopl.library.lending.patronprofile.model;

import io.pillopl.library.lending.patron.model.PatronId;

@FunctionalInterface
public interface PatronProfiles {

    PatronProfile fetchFor(PatronId patronId);

}


