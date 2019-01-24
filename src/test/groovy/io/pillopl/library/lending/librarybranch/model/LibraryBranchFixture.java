package io.pillopl.library.lending.librarybranch.model;

import java.util.UUID;

public class LibraryBranchFixture {

    public static LibraryBranchId anyBranch() {
        return new LibraryBranchId(UUID.randomUUID());
    }
}
