package io.pillopl.library.lending.domain.library;

import lombok.Value;

import java.util.UUID;

@Value
//TODO add not null
public class LibraryBranchId {

    final UUID libraryBranchId;
}
