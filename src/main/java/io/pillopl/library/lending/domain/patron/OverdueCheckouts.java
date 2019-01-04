package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import lombok.NonNull;
import lombok.Value;

import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

@Value
public class OverdueCheckouts {

    @NonNull Map<LibraryBranchId, Set<OverdueCheckout>> overdueCheckouts;

    int countAt(LibraryBranchId libraryBranchId) {
        return overdueCheckouts.getOrDefault(libraryBranchId, emptySet()).size();
    }

}
