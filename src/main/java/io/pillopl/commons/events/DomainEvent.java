package io.pillopl.commons.events;

import java.util.UUID;

public interface DomainEvent {

    UUID getEventId();
}
