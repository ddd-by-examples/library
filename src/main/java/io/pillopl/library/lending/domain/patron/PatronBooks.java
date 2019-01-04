package io.pillopl.library.lending.domain.patron;


import io.pillopl.library.lending.domain.book.AvailableBook;
import io.pillopl.library.lending.domain.book.BookOnHold;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.*;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;

import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents.events;
import static io.pillopl.library.lending.domain.patron.PatronHolds.MAX_NUMBER_OF_HOLDS;
import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;

//TODO - rename to patron?
@AllArgsConstructor
@EqualsAndHashCode(of = "patron")
public class PatronBooks {

    private final PatronInformation patron;

    private final List<PlacingOnHoldPolicy> placingOnHoldPolicies;

    private final OverdueCheckouts overdueCheckouts;

    private final PatronHolds patronHolds;

    public Either<BookHoldFailed, BookPlacedOnHoldEvents> placeOnHold(AvailableBook book) {
        return placeOnHold(book, HoldDuration.forOpenEnded());
    }

    public Either<BookHoldFailed, BookPlacedOnHoldEvents> placeOnHold(AvailableBook aBook, HoldDuration forDuration) {
        Option<Rejection> rejection = patronCanHold(aBook, forDuration);
        if (rejection.isEmpty()) {
            BookPlacedOnHold bookPlacedOnHold = BookPlacedOnHold.now(aBook.getBookInformation(), aBook.getLibraryBranch(), patron, forDuration);
            if (patronHolds.maximumHoldsAfterHolding(aBook)) {
                return right(events(patron, bookPlacedOnHold, MaximumNumberOhHoldsReached.now(patron, MAX_NUMBER_OF_HOLDS)));
            }
            return right(events(patron, bookPlacedOnHold));
        }
        return left(BookHoldFailed.now(rejection.get(), aBook.getBookId(), aBook.getLibraryBranch(), patron));
    }

    public Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold(BookOnHold book) {
        if (patronHolds.a(book)) {
            return right(BookHoldCanceled.now(book.getBookId(), book.getHoldPlacedAt(), patron));
        }
        return left(BookHoldCancelingFailed.now(book.getBookId(), book.getHoldPlacedAt(), patron));
    }

    public Either<BookCollectingFailed, BookCollected> collect(BookOnHold book) {
        if (patronHolds.a(book)) {
            return right(BookCollected.now(book.getBookInformation(), book.getHoldPlacedAt(), patron.getPatronId()));
        }
        return left(BookCollectingFailed.now(Rejection.withReason("book is not on hold by patron"), book.getBookId(), book.getHoldPlacedAt(), patron));
    }

    private Option<Rejection> patronCanHold(AvailableBook aBook, HoldDuration forDuration) {
        return placingOnHoldPolicies
                .toStream()
                .map(policy -> policy.apply(aBook, this, forDuration))
                .find(Either::isLeft)
                .map(Either::getLeft);
    }

    boolean isRegular() {
        return patron.isRegular();
    }

    int overdueCheckoutsAt(LibraryBranchId libraryBranch) {
        return overdueCheckouts.countAt(libraryBranch);
    }

    public int numberOfHolds() {
        return patronHolds.count();
    }

}


