package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.library.model.LibraryBranchId;
import lombok.NonNull;
import lombok.Value;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

@Value
public class OverdueCheckouts {

    static int MAX_COUNT_OF_OVERDUE_RESOURCES = 2;

    @NonNull Map<LibraryBranchId, Set<OverdueCheckout>> overdueCheckouts;

    int countAt(LibraryBranchId libraryBranchId) {
        return overdueCheckouts.getOrDefault(libraryBranchId, emptySet()).size();
    }

}



