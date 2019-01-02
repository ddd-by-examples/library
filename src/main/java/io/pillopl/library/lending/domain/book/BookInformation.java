package io.pillopl.library.lending.domain.book;


import lombok.Value;

@Value
public class BookInformation {

    BookId bookId;
    BookType bookType;
}
