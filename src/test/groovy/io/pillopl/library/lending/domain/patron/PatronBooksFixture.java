package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;

import java.util.*;
import java.util.stream.Collectors;

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId;
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch;
import static io.pillopl.library.lending.domain.patron.PlacingOnHoldPolicy.allCurrentPolicies;
import static java.util.stream.IntStream.rangeClosed;

public class PatronBooksFixture {

    public static PatronBooks regularPatron() {
        return regularPatron(anyPatronId());
    }

    public static PatronBooks regularPatron(PatronId patronId) {
        return new PatronBooks(
                patronInformation(patronId, PatronInformation.PatronType.Regular),
                allCurrentPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                noHolds());
    }

    static PatronInformation patronInformation(PatronId id, PatronInformation.PatronType type) {
        return new PatronInformation(id, type);
    }

    public static PatronBooks regularPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronBooks(
                patronInformation(patronId, PatronInformation.PatronType.Regular),
                allCurrentPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                booksOnHold(numberOfHolds));
    }

    static PatronBooks regularPatronWith(PatronHold patronHold) {
        PatronId patronId = anyPatronId();
        PatronHolds patronHolds = new PatronHolds(Collections.singleton(patronHold));
        return new PatronBooks(
                patronInformation(patronId, PatronInformation.PatronType.Regular),
                allCurrentPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                patronHolds);
    }

    public static PatronHold onHold() {
        return new PatronHold(anyBookId(), anyBranch());
    }

    static PatronHolds booksOnHold(int numberOfHolds) {
        return new PatronHolds(rangeClosed(1, numberOfHolds)
                .mapToObj(i -> new PatronHold(anyBookId(), anyBranch()))
                .collect(Collectors.toSet()));
    }

    static PatronBooks researcherPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronBooks(
                patronInformation(patronId, PatronInformation.PatronType.Researcher),
                allCurrentPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                booksOnHold(numberOfHolds));
    }

    static PatronBooks regularPatronWithOverdueCheckouts(LibraryBranchId libraryBranchId, Set<BookId> overdueResources) {
        Map<LibraryBranchId, Set<BookId>> overdueCheckouts = new HashMap<>();
        overdueCheckouts.put(libraryBranchId, overdueResources);
        return new PatronBooks(
                patronInformation(anyPatronId(), PatronInformation.PatronType.Regular),
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

    static PatronHolds noHolds() {
        return new PatronHolds(new HashSet<>());
    }



}
