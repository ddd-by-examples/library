package io.pillopl.library.domain;

import lombok.Value;

import java.util.UUID;

@Value
//TODO add not null
class PatronInformation {

    enum PatronType {RESEARCHER, REGULAR}

    PatronId patronId;

    PatronType type;

    boolean isRegular() {
        return type.equals(PatronType.REGULAR);
    }
}

@Value
//TODO add not null
class PatronId {
    UUID patronId;
}
