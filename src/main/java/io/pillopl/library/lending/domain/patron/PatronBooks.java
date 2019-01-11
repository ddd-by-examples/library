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

import static io.pillopl.commons.commands.EitherResult.announceFailure;
import static io.pillopl.commons.commands.EitherResult.announceSuccess;
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollected.bookCollectedNow;
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollectingFailed.bookCollectingFailedNow;
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldCanceled.*;
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldCancelingFailed.*;
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldFailed.bookHoldFailedNow;
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold.bookPlacedOnHoldNow;
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHoldEvents.events;
import static io.pillopl.library.lending.domain.patron.PatronHolds.MAX_NUMBER_OF_HOLDS;
import static io.pillopl.library.lending.domain.patron.Rejection.withReason;
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
        return placeOnHold(book, HoldDuration.openEnded());
    }

    public Either<BookHoldFailed, BookPlacedOnHoldEvents> placeOnHold(AvailableBook aBook, HoldDuration duration) {
        Option<Rejection> rejection = patronCanHold(aBook, duration);
        if (rejection.isEmpty()) {
            BookPlacedOnHold bookPlacedOnHold = bookPlacedOnHoldNow(aBook.getBookInformation(), aBook.getLibraryBranch(), patron, duration);
            if (patronHolds.maximumHoldsAfterHolding(aBook)) {
                return announceSuccess(events(bookPlacedOnHold, MaximumNumberOhHoldsReached.now(patron, MAX_NUMBER_OF_HOLDS)));
            }
            return right(events(bookPlacedOnHold));
        }
        return announceFailure(bookHoldFailedNow(rejection.get(), aBook.getBookId(), aBook.getLibraryBranch(), patron));
    }

    public Either<BookHoldCancelingFailed, BookHoldCanceled> cancelHold(BookOnHold book) {
        if (patronHolds.a(book)) {
            return announceSuccess(holdCanceledNow(book.getBookInformation(), book.getHoldPlacedAt(), patron));
        }
        return announceFailure(holdCancelingFailedNow(book.getBookId(), book.getHoldPlacedAt(), patron));
    }

    public Either<BookCollectingFailed, BookCollected> collect(BookOnHold book, CheckoutDuration duration) {
        if (patronHolds.a(book)) {
            return announceSuccess(bookCollectedNow(book.getBookInformation(), book.getHoldPlacedAt(), patron.getPatronId(), duration));
        }
        return announceFailure(bookCollectingFailedNow(withReason("book is not on hold by patron"), book.getBookId(), book.getHoldPlacedAt(), patron));
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


