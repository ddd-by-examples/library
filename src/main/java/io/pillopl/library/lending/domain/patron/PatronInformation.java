package io.pillopl.library.lending.domain.patron;

import lombok.NonNull;
import lombok.Value;

import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular;

@Value
public class PatronInformation {

    public enum PatronType {Researcher, Regular}

    @NonNull PatronId patronId;

    @NonNull PatronType type;

    boolean isRegular() {
        return type.equals(Regular);
    }
}

