package io.pillopl.library.lending.domain.book;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class BookId {

    @NonNull UUID bookId;
}
