package io.pillopl.library.lending.domain.patron;

import lombok.Value;

import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular;

@Value
//TODO add not null
public class PatronInformation {

    public enum PatronType {Researcher, Regular}

    PatronId patronId;

    PatronType type;

    boolean isRegular() {
        return type.equals(Regular);
    }
}

