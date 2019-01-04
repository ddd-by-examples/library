package io.pillopl.library.lending.domain.book;

public interface Book {

    default BookId bookId() {
        return getBookInformation().getBookId();
    }

    BookInformation getBookInformation();

}

