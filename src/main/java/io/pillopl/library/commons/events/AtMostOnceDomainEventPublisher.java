package io.pillopl.library.commons.events;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;

@AllArgsConstructor
public class AtMostOnceDomainEventPublisher implements DomainEvents {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(DomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
