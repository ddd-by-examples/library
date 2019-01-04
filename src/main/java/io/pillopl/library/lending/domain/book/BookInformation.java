package io.pillopl.library.lending.domain.book;


import lombok.NonNull;
import lombok.Value;

@Value
public class BookInformation {

    @NonNull
    BookId bookId;

    @NonNull
    BookType bookType;
}
