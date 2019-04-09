package io.pillopl.library.lending.patron.model;

import io.vavr.control.Option;

public interface PatronRepository {

    Option<Patron> findBy(PatronId patronId);

    Patron publish(PatronEvent event);
}
