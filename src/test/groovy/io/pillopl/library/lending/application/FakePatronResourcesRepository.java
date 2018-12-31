package io.pillopl.library.lending.application;

import io.pillopl.library.lending.domain.patron.*;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FakePatronResourcesRepository implements PatronResourcesRepository {

    private final Map<PatronId, PatronResources> database = new ConcurrentHashMap<>();

    @Override
    public Option<PatronResources> findBy(PatronId patronId) {
        return Option.of(database.get(patronId));
    }

    @Override
    public Try<Void> reactTo(PatronResourcesEvent event) {
        return Try.run(() -> {
            database.put(event.patronId(), PatronResourcesFixture.regularPatron(event.patronId()));
        });
    }


}
