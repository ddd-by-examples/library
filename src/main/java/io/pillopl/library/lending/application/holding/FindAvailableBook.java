package io.pillopl.library.lending.application.holding;


import io.pillopl.library.lending.domain.book.AvailableBook;
import io.pillopl.library.lending.domain.book.BookId;
import io.vavr.control.Option;

@FunctionalInterface
interface FindAvailableBook {

    Option<AvailableBook> with(BookId bookId);
}
