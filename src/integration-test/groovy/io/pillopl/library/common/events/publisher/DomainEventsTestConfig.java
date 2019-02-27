package io.pillopl.library.common.events.publisher;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.commons.events.publisher.JustForwardDomainEventPublisher;
import io.pillopl.library.commons.events.publisher.StoreAndForwardDomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class DomainEventsTestConfig {

    @Bean
    @Primary
    DomainEvents domainEventsWithStorage(ApplicationEventPublisher applicationEventPublisher) {
        return new StoreAndForwardDomainEventPublisher(new JustForwardDomainEventPublisher(applicationEventPublisher),
                new InMemoryEventsStorage());
    }
}