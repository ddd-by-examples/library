package io.pillopl.library.lending.patron.model;

import io.vavr.control.Option;

public interface PatronBooksRepository {

    Option<PatronBooks> findBy(PatronId patronId);

    PatronBooks publish(PatronBooksEvent event);
}
