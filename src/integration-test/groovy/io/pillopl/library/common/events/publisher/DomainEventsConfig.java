package io.pillopl.library.common.events.publisher;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.commons.events.publisher.JustForwardDomainEventPublisher;
import io.pillopl.library.commons.events.publisher.StoreAndForwardDomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainEventsConfig {

    @Bean
    DomainEvents domainEvents(ApplicationEventPublisher applicationEventPublisher) {
        return new StoreAndForwardDomainEventPublisher(new JustForwardDomainEventPublisher(applicationEventPublisher), new InMemoryEventsStorage());
    }


}