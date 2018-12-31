package io.pillopl.library.lending.application;

import io.pillopl.library.lending.domain.patron.PatronId;
import io.pillopl.library.lending.domain.patron.PatronResources;
import io.pillopl.library.lending.domain.patron.PatronResourcesRepository;
import io.pillopl.library.lending.domain.patron.PatronResourcesSnapshot;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPatronResourcesRepository implements PatronResourcesRepository {

    private final Map<PatronId, PatronResources> database = new ConcurrentHashMap<>();

    @Override
    public Option<PatronResources> findBy(PatronId patronId) {
        return Option.of(database.get(patronId));
    }

    @Override
    public Try<PatronResources> save(PatronResources patronResources) {
        return Try.ofSupplier(() -> {
            PatronResourcesSnapshot snapshot = patronResources.toSnapshot();
            return database.put(snapshot.getPatronInformation().getPatronId(), patronResources);
        });
    }
}
