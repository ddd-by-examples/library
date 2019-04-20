package io.pillopl.library.lending.patron.model;

import io.vavr.control.Option;

public interface Patrons {

    Option<Patron> findBy(PatronId patronId);

    Patron publish(PatronEvent event);
}
