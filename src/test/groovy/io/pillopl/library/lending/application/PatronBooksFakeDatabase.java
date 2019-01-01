package io.pillopl.library.lending.application;

import io.pillopl.library.lending.domain.patron.*;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PatronBooksFakeDatabase implements PatronBooksRepository {

    private final Map<PatronId, PatronBooks> database = new ConcurrentHashMap<>();

    @Override
    public Option<PatronBooks> findBy(PatronId patronId) {
        return Option.of(database.get(patronId));
    }

    @Override
    public Try<Void> reactTo(PatronBooksEvent event) {
        return Try.run(() -> {
            database.put(event.patronId(), PatronBooksFixture.regularPatron(event.patronId()));
        });
    }


}
