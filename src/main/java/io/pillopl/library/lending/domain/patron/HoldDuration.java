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

    boolean isOpenEnded() {
        return getTo().isEmpty();
    }

    Option<Instant> getTo() {
        return Option.of(to);
    }

    public static HoldDuration forOpenEnded() {
        return forOpenEnded(Instant.now());
    }

    public static HoldDuration forOpenEnded(Instant from) {
        return new HoldDuration(from, null);
    }

    public static HoldDuration forCloseEnded(int days) {
        return forCloseEnded(Instant.now(), days);
    }

    static HoldDuration forCloseEnded(Instant from, int days) {
        Instant till = from.plus(Duration.ofDays(days));
        return new HoldDuration(from, till);
    }
}
