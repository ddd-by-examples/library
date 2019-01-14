package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.book.model.BookId;
import lombok.NonNull;
import lombok.Value;

@Value
public class OverdueCheckout {
    @NonNull BookId overdueBook;

}
