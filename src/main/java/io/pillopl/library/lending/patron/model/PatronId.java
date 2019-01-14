package io.pillopl.library.lending.patron.model;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class PatronId {
    @NonNull UUID patronId;
}
