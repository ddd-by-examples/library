package io.pillopl.library.lending.domain.library;

import lombok.Value;

import java.util.UUID;

@Value
//TODO add not null
//TODO bump project to Java 11/9
public class LibraryBranchId {

    final UUID libraryBranchId;
}
