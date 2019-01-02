package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import lombok.AllArgsConstructor;
import lombok.Value;

//TODO add not null
@Value
@AllArgsConstructor
public class PatronHold {

    BookId bookId;
    LibraryBranchId libraryBranchId;

}
