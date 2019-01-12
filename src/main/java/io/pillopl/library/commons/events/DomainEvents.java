package io.pillopl.library.commons.events;

import io.vavr.control.Try;

public interface DomainEvents {

    Try<Void> publish(DomainEvent event);
}
