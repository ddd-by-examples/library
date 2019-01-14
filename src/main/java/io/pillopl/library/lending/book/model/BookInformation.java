package io.pillopl.library.lending.book.model;


import lombok.NonNull;
import lombok.Value;

@Value
public class BookInformation {

    @NonNull
    BookId bookId;

    @NonNull
    BookType bookType;
}
