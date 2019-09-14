package io.pillopl.library.commons.events.publisher;

import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.commons.events.DomainEvents;
import io.vavr.collection.List;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;


@AllArgsConstructor
public class StoreAndForwardDomainEventPublisher implements DomainEvents {

    private final DomainEvents eventsPublisher;
    private final EventsStorage eventsStorage;

    @Override
    public void publish(DomainEvent event) {
        eventsStorage.save(event);
    }

    @Scheduled(fixedRate = 3000L)
    @Transactional
    public void publishAllPeriodically() {
        List<DomainEvent> domainEvents = eventsStorage.toPublish();
        domainEvents.forEach(eventsPublisher::publish);
        eventsStorage.published(domainEvents);
    }
}
