package io.pillopl.library.lending.infrastructure.checkout;

import io.pillopl.library.lending.application.hold.FindHoldsToExpirePolicy;
import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.pillopl.library.lending.infrastructure.patron.PatronBooksEntityRepository;
import io.vavr.Tuple;
import lombok.AllArgsConstructor;

import java.time.Clock;
import java.time.Instant;

import static io.vavr.collection.List.ofAll;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
class FindExpiredHoldsInDatabaseByHoldDuration implements FindHoldsToExpirePolicy {

    private final PatronBooksEntityRepository patronBooksEntityRepository;
    private final Clock clock;

    @Override
    public HoldsToExpire allHoldsToExpire() {
        return new HoldsToExpire(
                ofAll(
                patronBooksEntityRepository
                        .findHoldsExpiredAt(Instant.now(clock))
                        .stream()
                        .map(entity -> Tuple.of(
                                new BookId(entity.getBookId()),
                                new PatronId(entity.getPatronId()),
                                new LibraryBranchId(entity.getLibraryBranchId())))
                        .collect(toList())));
    }
}
