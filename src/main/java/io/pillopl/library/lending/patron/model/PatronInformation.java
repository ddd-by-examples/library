package io.pillopl.library.lending.patron.model;

import lombok.NonNull;
import lombok.Value;

import static io.pillopl.library.lending.patron.model.PatronType.Regular;

@Value
class PatronInformation {

    @NonNull PatronId patronId;

    @NonNull PatronType type;

    boolean isRegular() {
        return type.equals(Regular);
    }
}

