package io.pillopl.library.common.events.publisher;

import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.commons.events.publisher.EventsStorage;
import io.vavr.collection.List;

import java.util.ArrayList;
import java.util.Collections;

public class InMemoryEventsStorage implements EventsStorage {

    //it's not thread safe, enough for testing
    private final java.util.List<DomainEvent> eventList = Collections.synchronizedList(new ArrayList<>());

    @Override
    synchronized public void save(DomainEvent event) {
        eventList.add(event);
    }

    @Override
    synchronized public List<DomainEvent> toPublish() {
        return List.ofAll(eventList);
    }

    @Override
    synchronized public void published(List<DomainEvent> events) {
        eventList.removeAll(events.asJava());
    }
}
