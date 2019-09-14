package io.pillopl.library.common.events.publisher

import groovy.transform.CompileStatic
import io.micrometer.core.instrument.MeterRegistry
import io.pillopl.library.LibraryApplication
import io.pillopl.library.commons.events.DomainEvent
import io.pillopl.library.commons.events.publisher.MeteredDomainEventPublisher
import io.pillopl.library.lending.LendingTestContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.awt.event.TextEvent
import java.time.Instant

@SpringBootTest(classes = [LendingTestContext.class, DomainEventsTestConfig.class])
class MeteredDomainEventPublisherSpec extends Specification {

    @Autowired
    MeterRegistry meterRegistry

    @Autowired
    MeteredDomainEventPublisher publisher

    def "should count published events"() {
        when:
            publisher.publish(new TestEvent())
        then:
            countedEvents("domain_events", "name", "TestEvent") == 1.0
        when:
            publisher.publish(new TestEvent())
        then:
            countedEvents("domain_events", "name", "TestEvent") == 2.0
    }

    private double countedEvents(String metricName, String key, String tag) {
        meterRegistry.counter(metricName, key, tag).count()
    }
}

@CompileStatic
class TestEvent implements DomainEvent {

    @Override
    UUID getEventId() {
        return null
    }

    @Override
    UUID getAggregateId() {
        return null
    }

    @Override
    Instant getWhen() {
        return null
    }
}