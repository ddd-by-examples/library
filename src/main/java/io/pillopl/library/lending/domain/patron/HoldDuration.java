package io.pillopl.library.lending.domain.patron;

import io.vavr.control.Option;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;

@Value
public class HoldDuration {

    Instant from;
    Instant to;

    private HoldDuration(Instant from, Instant to) {
        this.from = from;
        this.to = to;
    }

    Option<Instant> activeTill() {
        return Option.of(to);
    }

    public static HoldDuration openEnded() {
        return new HoldDuration(Instant.now(), null);
    }

    public static HoldDuration closeEnded(int days) {
        Instant now = Instant.now();
        Instant till = now.plus(Duration.ofDays(days));
        return new HoldDuration(Instant.now(), till);
    }
}
