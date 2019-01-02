package io.pillopl.library.lending.domain.book;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.BookOnHold;

import java.util.UUID;

import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch;
import static io.pillopl.library.lending.domain.book.Book.BookState.Available;
import static io.pillopl.library.lending.domain.book.Book.BookState.Collected;
import static io.pillopl.library.lending.domain.book.Book.BookState.OnHold;
import static io.pillopl.library.lending.domain.book.Book.BookType.Circulating;
import static io.pillopl.library.lending.domain.book.Book.BookType.Restricted;

public class BookFixture {

    public static Book bookOnHold(BookId bookId, LibraryBranchId libraryBranchId) {
        return new Book(bookId, libraryBranchId, Circulating, OnHold);
    }

    public static Book circulatingBook() {
        return new Book(anyBookId(), anyBranch(), Circulating, Available);
    }

    public static Book bookOnHold() {
        return new Book(anyBookId(), anyBranch(), Circulating, OnHold);
    }

    public static BookOnHold onHold() {
        return new BookOnHold(anyBookId(), anyBranch());
    }

    public static Book circulatingResourceAt(LibraryBranchId libraryBranchId) {
        return new Book(anyBookId(), libraryBranchId, Circulating, Available);
    }

    static Book collectedBook() {
        return new Book(anyBookId(), anyBranch(), Circulating, Collected);
    }

    public static Book restrictedBook() {
        return new Book(anyBookId(), anyBranch(), Restricted, Available);
    }

    public static BookId anyBookId() {
        return new BookId(UUID.randomUUID());
    }


}
