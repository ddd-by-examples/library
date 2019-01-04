package io.pillopl.library.lending.domain.book;

import io.vavr.control.Option;
import io.vavr.control.Try;

public interface BookRepository {

    Option<Book> findBy(BookId bookId);

    Try<Void> save(Book book);
}
