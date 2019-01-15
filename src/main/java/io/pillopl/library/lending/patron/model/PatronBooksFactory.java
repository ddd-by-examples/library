package io.pillopl.library.lending.patron.model;

import io.pillopl.library.lending.book.model.BookId;
import io.pillopl.library.lending.library.model.LibraryBranchId;
import io.vavr.Tuple2;

import java.util.Map;
import java.util.Set;

import static io.pillopl.library.lending.patron.model.PlacingOnHoldPolicy.allCurrentPolicies;
import static java.util.stream.Collectors.toSet;

public class PatronBooksFactory {

    public PatronBooks recreateFrom(PatronType patronType, PatronId patronId, Set<Tuple2<BookId, LibraryBranchId>> patronHolds, Map<LibraryBranchId, Set<BookId>> overdueCheckouts) {
        return new PatronBooks(new PatronInformation(patronId, patronType),
                allCurrentPolicies(),
                new OverdueCheckouts(overdueCheckouts),
                new PatronHolds(
                        patronHolds
                                .stream()
                                .map(tuple -> new Hold(tuple._1, tuple._2))
                                .collect(toSet())));
    }

}
