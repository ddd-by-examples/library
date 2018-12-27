package io.pillopl.books.domain;

import lombok.Value;

import java.util.UUID;

@Value
class PatronInformation {

    enum PatronType {RESEARCHER, REGULAR}

    PatronId patronId;

    PatronType type;

    boolean isRegular() {
        return type.equals(PatronType.REGULAR);
    }
}

@Value
class PatronId {
    UUID patronId;
}
