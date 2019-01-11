package io.pillopl.library.common.events;

import io.vavr.control.Try;

public interface DomainEvents {

    Try<Void> publish(DomainEvent event);
}
