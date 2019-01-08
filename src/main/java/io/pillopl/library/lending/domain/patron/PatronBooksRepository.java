package io.pillopl.library.lending.domain.patron;

import io.vavr.control.Option;
import io.vavr.control.Try;

public interface PatronBooksRepository {

    Option<PatronBooks> findBy(PatronId patronId);

    Try<PatronBooks> handle(PatronBooksEvent event);
}
