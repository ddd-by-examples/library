package io.pillopl.library.commons.events.publisher;

import io.micrometer.core.instrument.MeterRegistry;
import io.pillopl.library.commons.events.DomainEvent;
import io.pillopl.library.commons.events.DomainEvents;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class MeteredDomainEventPublisher implements DomainEvents {

    private static final String DOMAIN_EVENTS = "domain_events";
    private static final String TAG_NAME = "name";

    private final DomainEvents delegate;
    private final MeterRegistry metricsRegistry;

    @Override
    public void publish(DomainEvent event) {
        delegate.publish(event);
        metricsRegistry.counter(DOMAIN_EVENTS, TAG_NAME, event.getClass().getSimpleName()).increment();
    }
}
