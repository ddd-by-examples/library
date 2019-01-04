package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.book.BookId;
import lombok.NonNull;
import lombok.Value;

@Value
public class OverdueCheckout {
    @NonNull BookId overdueBook;

}
