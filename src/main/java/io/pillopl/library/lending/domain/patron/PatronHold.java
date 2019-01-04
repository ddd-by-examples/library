package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import lombok.NonNull;
import lombok.Value;

@Value
public class PatronHold {

    @NonNull BookId bookId;
    @NonNull LibraryBranchId libraryBranchId;

}
