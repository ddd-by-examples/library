package io.pillopl.library.lending.domain.book;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.BookOnHold;

import java.util.UUID;

import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch;
import static io.pillopl.library.lending.domain.book.Book.BookState.AVAILABLE;
import static io.pillopl.library.lending.domain.book.Book.BookState.COLLECTED;
import static io.pillopl.library.lending.domain.book.Book.BookState.ON_HOLD;
import static io.pillopl.library.lending.domain.book.Book.BookType.CIRCULATING;
import static io.pillopl.library.lending.domain.book.Book.BookType.RESTRICTED;

public class BookFixture {

    public static Book bookOnHold(BookId bookId, LibraryBranchId libraryBranchId) {
        return new Book(bookId, libraryBranchId, CIRCULATING, ON_HOLD);
    }

    public static Book circulatingBook() {
        return new Book(anyBookId(), anyBranch(), CIRCULATING, AVAILABLE);
    }

    public static Book bookOnHold() {
        return new Book(anyBookId(), anyBranch(), CIRCULATING, ON_HOLD);
    }

    public static BookOnHold onHold() {
        return new BookOnHold(anyBookId(), anyBranch());
    }

    public static Book circulatingResourceAt(LibraryBranchId libraryBranchId) {
        return new Book(anyBookId(), libraryBranchId, CIRCULATING, AVAILABLE);
    }

    static Book collectedBook() {
        return new Book(anyBookId(), anyBranch(), CIRCULATING, COLLECTED);
    }

    public static Book restrictedBook() {
        return new Book(anyBookId(), anyBranch(), RESTRICTED, AVAILABLE);
    }

    public static BookId anyBookId() {
        return new BookId(UUID.randomUUID());
    }


}
