package io.pillopl.library.lending.library.model;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class LibraryBranchId {

    @NonNull UUID libraryBranchId;
}
