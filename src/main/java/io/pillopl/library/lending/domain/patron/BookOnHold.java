package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.book.Book;
import io.pillopl.library.lending.domain.book.BookId;
import lombok.AllArgsConstructor;
import lombok.Value;

//TODO add not null
@Value
@AllArgsConstructor
public class BookOnHold {

    BookId bookId;
    LibraryBranchId libraryBranchId;

    BookOnHold(Book book) {
        this(book.getBookId(), book.getLibraryBranch());
    }

}
