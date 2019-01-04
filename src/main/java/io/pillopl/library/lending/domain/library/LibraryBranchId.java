package io.pillopl.library.lending.domain.library;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class LibraryBranchId {

    @NonNull UUID libraryBranchId;
}
