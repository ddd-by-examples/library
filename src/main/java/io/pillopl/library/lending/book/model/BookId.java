package io.pillopl.library.lending.book.model;

import lombok.NonNull;
import lombok.Value;

import java.util.UUID;

@Value
public class BookId {

    @NonNull
    UUID bookId;
}
