package io.pillopl.library.lending.domain.patron;

import lombok.Value;

@Value
//TODO add not null
public class PatronInformation {

    public enum PatronType {RESEARCHER, REGULAR}

    PatronId patronId;

    PatronType type;

    boolean isRegular() {
        return type.equals(PatronType.REGULAR);
    }
}

