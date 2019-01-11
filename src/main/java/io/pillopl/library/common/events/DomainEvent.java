package io.pillopl.library.common.events;

import java.util.UUID;

public interface DomainEvent {

    UUID getEventId();
}
