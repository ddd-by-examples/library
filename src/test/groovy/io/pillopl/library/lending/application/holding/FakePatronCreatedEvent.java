package io.pillopl.library.lending.application.holding;

import io.pillopl.library.lending.domain.patron.PatronBooksEvent;
import io.pillopl.library.lending.domain.patron.PatronId;
import lombok.Value;

import java.util.UUID;

@Value
public class FakePatronCreatedEvent implements PatronBooksEvent {

    UUID patronId;

    public FakePatronCreatedEvent(PatronId patronId) {
        this.patronId = patronId.getPatronId();
    }

}
