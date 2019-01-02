package io.pillopl.library.lending.application;

import io.pillopl.library.lending.application.holding.FakePatronCreatedEvent;
import io.pillopl.library.lending.application.holding.FakeTooManyHoldsEvent;
import io.pillopl.library.lending.domain.patron.*;
import io.vavr.Predicates;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

public class PatronBooksFakeDatabase implements PatronBooksRepository {

    private final Map<PatronId, PatronBooks> database = new ConcurrentHashMap<>();

    @Override
    public Option<PatronBooks> findBy(PatronId patronId) {
        return Option.of(database.get(patronId));
    }

    @Override
    public Try<Void> reactTo(PatronBooksEvent event) {
        return Try.run(() -> {
            Match(event).of(
                    Case($(Predicates.instanceOf(FakePatronCreatedEvent.class)), this::createRegular),
                    Case($(Predicates.instanceOf(FakeTooManyHoldsEvent.class)), this::createWithManyHolds)
            );
        });
    }


    private FakePatronCreatedEvent createRegular(FakePatronCreatedEvent event) {
        database.put(event.patronId(), PatronBooksFixture.regularPatron(event.patronId()));
        return event;
    }

    private FakeTooManyHoldsEvent createWithManyHolds(FakeTooManyHoldsEvent event) {
        database.put(event.patronId(), PatronBooksFixture.regularPatronWithHolds(100));
        return event;
    }


}
