package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.book.model.BookId;
import io.pillopl.library.lending.library.model.LibraryBranchId;
import lombok.NonNull;
import lombok.Value;

@Value
class Hold {

    @NonNull BookId bookId;
    @NonNull LibraryBranchId libraryBranchId;

}
