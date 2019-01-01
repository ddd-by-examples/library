package io.pillopl.library.lending.domain.book;

import lombok.Value;

import java.util.UUID;

@Value
//TODO add not null
public class BookId {

    UUID bookId;
}
