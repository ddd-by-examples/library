package io.pillopl.library.lending.application;

import io.pillopl.library.lending.application.holding.FakeTooManyHoldsEvent;
import io.pillopl.library.lending.domain.patron.*;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.PatronCreated;
import io.vavr.Predicates;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.vavr.API.*;

public class PatronBooksFakeDatabase implements PatronBooksRepository {

    private final Map<PatronId, PatronBooks> database = new ConcurrentHashMap<>();

    @Override
    public Option<PatronBooks> findBy(PatronId patronId) {
        return Option.of(database.get(patronId));
    }

    @Override
    public Try<Void> handle(PatronBooksEvent event) {
        return Try.run(() -> {
            Match(event).of(
                    Case($(Predicates.instanceOf(PatronCreated.class)), this::createRegular),
                    Case($(Predicates.instanceOf(FakeTooManyHoldsEvent.class)), this::createWithManyHolds)
            );
        });
    }


    private PatronCreated createRegular(PatronCreated event) {
        database.put(event.patronId(), PatronBooksFixture.regularPatron(event.patronId()));
        return event;
    }

    private FakeTooManyHoldsEvent createWithManyHolds(FakeTooManyHoldsEvent event) {
        database.put(event.patronId(), PatronBooksFixture.regularPatronWithHolds(100));
        return event;
    }


}
