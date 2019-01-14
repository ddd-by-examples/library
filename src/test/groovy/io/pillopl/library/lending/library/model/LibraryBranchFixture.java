package io.pillopl.library.lending.library.model;

import java.util.UUID;

public class LibraryBranchFixture {

    public static LibraryBranchId anyBranch() {
        return new LibraryBranchId(UUID.randomUUID());
    }
}
