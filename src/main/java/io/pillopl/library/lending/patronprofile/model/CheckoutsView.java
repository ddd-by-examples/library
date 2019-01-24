package io.pillopl.library.lending.patronprofile.model;

import io.pillopl.library.catalogue.BookId;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import lombok.NonNull;
import lombok.Value;

import java.time.Instant;

@Value
public class CheckoutsView {

    @NonNull
    List<Tuple2<BookId, Instant>> currentCheckouts;

}
