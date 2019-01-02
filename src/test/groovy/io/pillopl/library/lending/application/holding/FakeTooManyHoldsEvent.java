package io.pillopl.library.lending.application.holding;

import io.pillopl.library.lending.domain.patron.PatronBooksEvent;
import io.pillopl.library.lending.domain.patron.PatronId;
import lombok.Value;

import java.util.UUID;

@Value
public class FakeTooManyHoldsEvent implements PatronBooksEvent {

    UUID patronId;

    public FakeTooManyHoldsEvent(PatronId patronId) {
        this.patronId = patronId.getPatronId();
    }


}
