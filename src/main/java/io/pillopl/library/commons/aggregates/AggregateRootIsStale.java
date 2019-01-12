package io.pillopl.library.commons.aggregates;

public class AggregateRootIsStale extends RuntimeException {

    public AggregateRootIsStale(String msg) {
        super(msg);
    }
}
