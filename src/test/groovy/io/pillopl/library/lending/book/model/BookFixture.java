package io.pillopl.library.lending.book.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.catalogue.BookType.Circulating;
import static io.pillopl.library.catalogue.BookType.Restricted;
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch;

public class BookFixture {

    public static BookOnHold bookOnHold(BookId bookId, LibraryBranchId libraryBranchId) {
        return new BookOnHold(new BookInformation(bookId, Circulating), libraryBranchId, anyPatronId(), Instant.now(), version0());
    }

    public static AvailableBook circulatingBook() {
        return new AvailableBook(new BookInformation(anyBookId(), Circulating), anyBranch(), version0());
    }

    public static BookOnHold bookOnHold() {
        return new BookOnHold(new BookInformation(anyBookId(), Circulating), anyBranch(), anyPatronId(), Instant.now(), version0());
    }

    public static AvailableBook circulatingAvailableBookAt(LibraryBranchId libraryBranchId) {
        return new AvailableBook(new BookInformation(anyBookId(), Circulating), libraryBranchId, version0());
    }

    public static AvailableBook circulatingAvailableBookAt(BookId bookId, LibraryBranchId libraryBranchId) {
        return new AvailableBook(new BookInformation(bookId, Circulating), libraryBranchId, version0());
    }

    public static AvailableBook aBookAt(LibraryBranchId libraryBranchId) {
        return new AvailableBook(new BookInformation(anyBookId(), Circulating), libraryBranchId, version0());
    }

    public static Version version0() {
        return new Version(0);
    }

    public static AvailableBook circulatingAvailableBook() {
        return circulatingAvailableBookAt(anyBranch());
    }

    public static CheckedOutBook checkedOutBook() {
        return new CheckedOutBook(new BookInformation(anyBookId(), Circulating), anyBranch(), anyPatronId(), version0());
    }

    public static AvailableBook restrictedBook() {
        return new AvailableBook(new BookInformation(anyBookId(), Restricted), anyBranch(), version0());
    }

    public static BookId anyBookId() {
        return new BookId(UUID.randomUUID());
    }


    private static PatronId anyPatronId() {
        return new PatronId(UUID.randomUUID());
    }


}
