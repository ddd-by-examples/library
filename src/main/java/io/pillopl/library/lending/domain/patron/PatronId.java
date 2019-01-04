package io.pillopl.library.lending.domain.patron;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class PatronId {
    @NonNull UUID patronId;
}
