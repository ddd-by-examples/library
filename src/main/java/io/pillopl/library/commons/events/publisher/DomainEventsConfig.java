package io.pillopl.library.commons.events.publisher;

import io.pillopl.library.commons.events.DomainEvents;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainEventsConfig {

    @Bean
    DomainEvents domainEvents(ApplicationEventPublisher applicationEventPublisher) {
        return new JustForwardDomainEventPublisher(applicationEventPublisher);
    }
}