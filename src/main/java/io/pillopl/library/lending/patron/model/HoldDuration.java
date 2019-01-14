package io.pillopl.library.lending.patron.model;

import io.vavr.control.Option;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;

@Value
public class HoldDuration {

    Instant from;
    Instant to;

    private HoldDuration(Instant from, Instant to) {
        if (to != null && to.isBefore(from)) {
            throw new IllegalStateException("Close-ended duration must be valid");
        }
        this.from = from;
        this.to = to;
    }

    boolean isOpenEnded() {
        return getTo().isEmpty();
    }

    Option<Instant> getTo() {
        return Option.of(to);
    }

    public static HoldDuration openEnded() {
        return openEnded(Instant.now());
    }

    public static HoldDuration openEnded(Instant from) {
        return new HoldDuration(from, null);
    }

    public static HoldDuration closeEnded(NumberOfDays days) {
        return closeEnded(Instant.now(), days);
    }

    public static HoldDuration closeEnded(int days) {
        return closeEnded(Instant.now(), NumberOfDays.of(days));
    }

    public static HoldDuration closeEnded(Instant from, NumberOfDays days) {
        Instant till = from.plus(Duration.ofDays(days.getDays()));
        return new HoldDuration(from, till);
    }

}


