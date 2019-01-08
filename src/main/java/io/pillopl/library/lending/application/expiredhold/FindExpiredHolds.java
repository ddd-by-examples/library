package io.pillopl.library.lending.application.expiredhold;


import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.vavr.Tuple3;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import lombok.Value;

@FunctionalInterface
public interface FindExpiredHolds {

    @Value
    class ExpiredHolds {

        List<Tuple3<BookId, PatronId, LibraryBranchId>> expiredHolds;

        Stream<Tuple3<BookId, PatronId, LibraryBranchId>> stream() {
            return expiredHolds.toStream();
        }
    }

    ExpiredHolds allExpiredHolds();
}
