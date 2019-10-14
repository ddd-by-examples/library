package io.pillopl.library.catalogue;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
class BookInstance {

    @NonNull
    ISBN bookIsbn;
    @NonNull
    BookId bookId;
    @NonNull
    BookType bookType;

    static BookInstance instanceOf(Book book, BookType bookType) {
        return new BookInstance(book.getBookIsbn(), new BookId(UUID.randomUUID()), bookType);

    }
}
