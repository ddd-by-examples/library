package io.pillopl.library.commons.events.publisher;

import io.pillopl.library.commons.events.DomainEvent;
import io.vavr.collection.List;

public interface EventsStorage {

    void save(DomainEvent event);

    List<DomainEvent> toPublish();

    void published(List<DomainEvent> events);
}
