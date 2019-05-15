package io.pillopl.library.lending.patronprofile.model;

import io.pillopl.library.catalogue.BookId;
import lombok.Value;

import java.time.Instant;

@Value
public class Hold {

    private final BookId book;

    private final Instant till;

}
