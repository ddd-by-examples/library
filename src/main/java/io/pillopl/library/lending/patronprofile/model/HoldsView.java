package io.pillopl.library.lending.patronprofile.model;

import io.vavr.collection.List;
import lombok.NonNull;
import lombok.Value;

@Value
public class HoldsView {

    @NonNull
    List<Hold> currentHolds;

}
