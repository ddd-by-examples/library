package io.pillopl.library.lending.domain.patron;

import lombok.Value;

import java.util.UUID;

@Value
//TODO add not null
public class PatronId {
    UUID patronId;
}
