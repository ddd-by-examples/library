package io.pillopl.library.lending.application.checkout;


import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.book.BookOnHold;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.vavr.control.Option;

@FunctionalInterface
public interface FindBookOnHold {

    Option<BookOnHold> findBookOnHold(BookId bookId, PatronId patronId);
}
