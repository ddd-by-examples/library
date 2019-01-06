package io.pillopl.library.catalogue;

import lombok.NonNull;
import lombok.Value;

@Value
public class Isbn {

    @NonNull
    //TODO add regex check ^(97(8|9))?\d{9}(\d|X)$
    String isbn;
}
