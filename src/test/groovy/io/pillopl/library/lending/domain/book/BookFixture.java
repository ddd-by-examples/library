package io.pillopl.library.lending.domain.book;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronId;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.lending.domain.book.BookType.Circulating;
import static io.pillopl.library.lending.domain.book.BookType.Restricted;
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch;

public class BookFixture {

    public static BookOnHold bookOnHold(BookId bookId, LibraryBranchId libraryBranchId) {
        return new BookOnHold(new BookInformation(bookId, Circulating), libraryBranchId, anyPatronId(), Instant.now());
    }

    public static AvailableBook circulatingBook() {
        return new AvailableBook(new BookInformation(anyBookId(), Circulating), anyBranch());
    }

    public static BookOnHold bookOnHold() {
        return new BookOnHold(new BookInformation(anyBookId(), Circulating), anyBranch(), anyPatronId(), Instant.now());
    }

    public static AvailableBook circulatingAvailableBookAt(LibraryBranchId libraryBranchId) {
        return new AvailableBook(new BookInformation(anyBookId(), Circulating), libraryBranchId);
    }

    public static AvailableBook circulatingAvailableBookAt(BookId bookId, LibraryBranchId libraryBranchId) {
        return new AvailableBook(new BookInformation(bookId, Circulating), libraryBranchId);
    }

    public static AvailableBook aBookAt(LibraryBranchId libraryBranchId) {
        return new AvailableBook(new BookInformation(anyBookId(), Circulating), libraryBranchId);
    }

    public static AvailableBook circulatingAvailableBook() {
        return circulatingAvailableBookAt(anyBranch());
    }

    public static CollectedBook collectedBook() {
        return new CollectedBook(new BookInformation(anyBookId(), Circulating), anyBranch(), anyPatronId());
    }

    public static AvailableBook restrictedBook() {
        return new AvailableBook(new BookInformation(anyBookId(), Restricted), anyBranch());
    }

    public static BookId anyBookId() {
        return new BookId(UUID.randomUUID());
    }


    private static PatronId anyPatronId() {
        return new PatronId(UUID.randomUUID());
    }


}
