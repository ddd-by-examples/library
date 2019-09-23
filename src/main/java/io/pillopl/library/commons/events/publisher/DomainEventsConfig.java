package io.pillopl.library.commons.events.publisher;

import io.micrometer.core.instrument.MeterRegistry;
import io.pillopl.library.commons.events.DomainEvents;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainEventsConfig {

    @Bean
    DomainEvents domainEvents(ApplicationEventPublisher applicationEventPublisher, MeterRegistry meterRegistry) {
        return new MeteredDomainEventPublisher(new JustForwardDomainEventPublisher(applicationEventPublisher), meterRegistry);
    }
}
