package io.pillopl.library.lending.domain.patron;


import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollected;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollectingFailed;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookHoldFailed;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold;
import io.pillopl.library.lending.domain.book.Book;
import io.pillopl.library.lending.domain.book.BookId;
import io.vavr.collection.List;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static io.pillopl.library.lending.domain.patron.Reason.withReason;
import static io.vavr.control.Either.left;
import static io.vavr.control.Either.right;
import static java.util.Collections.emptySet;

//TODO - open/close
//TODO - rename to patron?
//TODO play with types
@AllArgsConstructor
@EqualsAndHashCode(of = "patron")
public class PatronBooks {

    private final PatronInformation patron;

    private final List<PlacingOnHoldPolicy> placingOnHoldPolicies;

    private final OverdueCheckouts overdueCheckouts;

    private final BooksOnHold booksOnHold;

    //TODO should pass BookRequest here?
    public Either<BookHoldFailed, BookPlacedOnHold> placeOnHold(Book book) {
        if(!book.isAvailable()) {
            return left(BookHoldFailed.now(new Reason("book is not available"), book.getBookId(), book.getLibraryBranch(), patron));
        }
        Option<Rejection> rejection = tryPlacingOnHold(book);
        if (!rejection.isEmpty()) {
            return left(BookHoldFailed.now(rejection.get().getReason(), book.getBookId(), book.getLibraryBranch(), patron));
        }
        return right(BookPlacedOnHold.now(book.getBookId(), book.getLibraryBranch(), patron));
    }

    public Either<BookCollectingFailed, BookCollected> collect(Book book) {
        BookOnHold bookToCollect = new BookOnHold(book);
        if (booksOnHold.doesNotContain(bookToCollect)) {
            return left(BookCollectingFailed.now(withReason("book is not on hold by patron"), book.getBookId(), book.getLibraryBranch(), patron));
        }
        return right(BookCollected.now(bookToCollect.getBookId(), bookToCollect.getLibraryBranchId(), patron.getPatronId()));
    }

    private Option<Rejection> tryPlacingOnHold(Book book) {
        return placingOnHoldPolicies
                .toStream()
                .map(policy -> policy.apply(book, this))
                .find(Either::isLeft)
                .map(Either::getLeft);
    }

    boolean isRegular() {
        return patron.isRegular();
    }

    int overdueCheckoutsAt(LibraryBranchId libraryBranch) {
        return overdueCheckouts.countAt(libraryBranch);
    }

    int numberOfHolds() {
        return booksOnHold.count();
    }

}

@Value
//TODO add not null
class OverdueCheckouts {

    Map<LibraryBranchId, Set<BookId>> overdueCheckouts;

    static OverdueCheckouts noOverdueCheckouts() {
        return new OverdueCheckouts(new HashMap<>());
    }

    int countAt(LibraryBranchId libraryBranchId) {
        return overdueCheckouts.getOrDefault(libraryBranchId, emptySet()).size();
    }

}


