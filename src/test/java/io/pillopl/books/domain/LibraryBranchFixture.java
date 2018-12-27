package io.pillopl.books.domain;

import java.util.UUID;

class LibraryBranchFixture {

    static LibraryBranchId anyBranch() {
        return new LibraryBranchId(UUID.randomUUID());
    }
}
