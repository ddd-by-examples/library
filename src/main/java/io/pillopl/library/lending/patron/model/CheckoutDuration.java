package io.pillopl.library.lending.patron.model;

import lombok.NonNull;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;

import static io.pillopl.library.lending.patron.model.NumberOfDays.of;
import static java.time.Instant.now;

@Value
public class CheckoutDuration {

    static final int MAX_CHECKOUT_DURATION  = 60;

    @NonNull NumberOfDays noOfDays;
    @NonNull Instant from;

    private CheckoutDuration(Instant from, NumberOfDays noOfDays) {
        if(noOfDays.isGreaterThan(MAX_CHECKOUT_DURATION)) {
            throw new IllegalArgumentException("Cannot checkout for more than " + MAX_CHECKOUT_DURATION + " days!");
        }
        this.noOfDays = noOfDays;
        this.from = from;
    }

    public static CheckoutDuration forNoOfDays(int noOfDays) {
        return forNoOfDays(now(), noOfDays);
    }

    static CheckoutDuration forNoOfDays(Instant from, int noOfDays) {
        return new CheckoutDuration(from, of(noOfDays));
    }

    public static CheckoutDuration maxDuration() {
        return forNoOfDays(MAX_CHECKOUT_DURATION);
    }

    Instant to() {
        return from.plus(Duration.ofDays(noOfDays.getDays()));
    }
}
