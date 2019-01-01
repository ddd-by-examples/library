package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.library.LibraryBranchFixture;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.book.BookFixture;
import io.pillopl.library.lending.domain.book.BookId;

import java.util.*;
import java.util.stream.Collectors;

import static io.pillopl.library.lending.domain.patron.PlacingOnHoldPolicy.allCurrentPolicies;
import static java.util.stream.IntStream.rangeClosed;

public class PatronBooksFixture {

    public static PatronBooks regularPatron() {
        return regularPatron(anyPatronId());
    }

    public static PatronBooks regularPatron(PatronId patronId) {
        return new PatronBooks(
                patronInformation(patronId, PatronInformation.PatronType.REGULAR),
                allCurrentPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                noHolds());
    }

    static PatronInformation patronInformation(PatronId id, PatronInformation.PatronType type) {
        return new PatronInformation(id, type);
    }

    static PatronBooks regularPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronBooks(
                patronInformation(patronId, PatronInformation.PatronType.REGULAR),
                allCurrentPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                booksOnHold(numberOfHolds));
    }

    static PatronBooks regularPatronWith(BookOnHold bookOnHold) {
        PatronId patronId = anyPatronId();
        BooksOnHold booksOnHold = new BooksOnHold(Collections.singleton(bookOnHold));
        return new PatronBooks(
                patronInformation(patronId, PatronInformation.PatronType.REGULAR),
                allCurrentPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                booksOnHold);
    }

    static BooksOnHold booksOnHold(int numberOfHolds) {
        return new BooksOnHold(rangeClosed(1, numberOfHolds)
                .mapToObj(i -> new BookOnHold(BookFixture.anyBookId(), LibraryBranchFixture.anyBranch()))
                .collect(Collectors.toSet()));
    }

    static PatronBooks researcherPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronBooks(
                patronInformation(patronId, PatronInformation.PatronType.RESEARCHER),
                allCurrentPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                booksOnHold(numberOfHolds));
    }

    static PatronBooks regularPatronWithOverdueCheckouts(LibraryBranchId libraryBranchId, Set<BookId> overdueResources) {
        Map<LibraryBranchId, Set<BookId>> overdueCheckouts = new HashMap<>();
        overdueCheckouts.put(libraryBranchId, overdueResources);
        return new PatronBooks(
                patronInformation(anyPatronId(), PatronInformation.PatronType.REGULAR),
                allCurrentPolicies(),
                new OverdueCheckouts(overdueCheckouts),
                noHolds());
    }

    public static PatronId anyPatronId() {
        return patronId(UUID.randomUUID());
    }

    static PatronId patronId(UUID patronId) {
        return new PatronId(patronId);
    }

    static BooksOnHold noHolds() {
        return new BooksOnHold(new HashSet<>());
    }



}
