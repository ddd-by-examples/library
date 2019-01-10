package io.pillopl.library.lending.domain.patron;

import lombok.Value;

@Value
public class NumberOfDays {

    int days;

    private NumberOfDays(int days) {
        if(days <= 0) {
            throw new IllegalArgumentException("Must pass positive integer as number of days");
        }
        this.days = days;
    }

    public static NumberOfDays of(int days) {
        return new NumberOfDays(days);
    }

    boolean isGreaterThan(int days) {
        return this.days > days;
    }
}
