package io.pillopl.library.lending.application.holding;


import io.pillopl.library.lending.domain.book.Book;
import io.pillopl.library.lending.domain.book.BookId;
import io.vavr.control.Option;

@FunctionalInterface
interface FindBook {

    Option<Book> with(BookId bookId);
}
