package io.pillopl.library.lending.book.application;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.Book;
import io.pillopl.library.lending.book.model.BookRepository;
import io.vavr.control.Option;

import java.util.HashMap;
import java.util.Map;

class InMemoryBookRepo implements BookRepository {

    Map<BookId, Book> books = new HashMap<>();

    @Override
    public Option<Book> findBy(BookId bookId) {
        return Option.of(books.get(bookId));
    }

    @Override
    public void save(Book book) {
        books.put(book.bookId(), book);
    }
}
