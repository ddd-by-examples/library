package io.pillopl.library.lending.patron.application.checkout;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.CheckoutDuration;
import io.pillopl.library.lending.patron.model.PatronId;
import java.time.Instant;
import lombok.NonNull;
import lombok.Value;

@Value
public class CheckOutBookCommand {
    @NonNull Instant timestamp;
    @NonNull PatronId patronId;
    @NonNull LibraryBranchId libraryId;
    @NonNull BookId bookId;
    @NonNull Integer noOfDays;

    public static CheckOutBookCommand create(PatronId patronId, LibraryBranchId libraryBranchId, BookId bookId, int noOfDays) {
        return new CheckOutBookCommand(Instant.now(), patronId, libraryBranchId, bookId, noOfDays);
    }

    CheckoutDuration getCheckoutDuration() {
        return CheckoutDuration.forNoOfDays(noOfDays);
    }
}
