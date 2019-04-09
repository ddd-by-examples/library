package io.pillopl.library.lending.patron.model;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.book.model.BookOnHold;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.vavr.collection.List;

import java.util.*;

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId;
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch;
import static io.pillopl.library.lending.patron.model.PatronType.Regular;
import static io.pillopl.library.lending.patron.model.PatronType.Researcher;
import static io.pillopl.library.lending.patron.model.PlacingOnHoldPolicy.*;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.rangeClosed;

public class PatronFixture {

    public static Patron regularPatron() {
        return regularPatron(anyPatronId());
    }

    public static Patron regularPatronWithPolicy(PlacingOnHoldPolicy placingOnHoldPolicy) {
        return patronWithPolicy(anyPatronId(), Regular, placingOnHoldPolicy);
    }

    public static Patron researcherPatronWithPolicy(PlacingOnHoldPolicy placingOnHoldPolicy) {
        return patronWithPolicy(anyPatronId(), Researcher, placingOnHoldPolicy);
    }

    public static Patron regularPatronWithPolicy(PatronId patronId, PlacingOnHoldPolicy placingOnHoldPolicy) {
        return patronWithPolicy(patronId, Regular, placingOnHoldPolicy);
    }

    public static Patron researcherPatronWithPolicy(PatronId patronId, PlacingOnHoldPolicy placingOnHoldPolicy) {
        return patronWithPolicy(patronId, Researcher, placingOnHoldPolicy);
    }

    private static Patron patronWithPolicy(PatronId patronId, PatronType type, PlacingOnHoldPolicy placingOnHoldPolicy) {
        return new Patron(patronInformation(patronId, type),
                List.of(placingOnHoldPolicy),
                new OverdueCheckouts(new HashMap<>()),
                noHolds());
    }

    public static Patron regularPatron(PatronId patronId) {
        return new Patron(
                patronInformation(patronId, Regular),
                List.of(onlyResearcherPatronsCanHoldRestrictedBooksPolicy),
                new OverdueCheckouts(new HashMap<>()),
                noHolds());
    }

    public static Patron researcherPatron(PatronId patronId) {
        return new Patron(
                patronInformation(patronId, Researcher),
                List.of(onlyResearcherPatronsCanHoldRestrictedBooksPolicy),
                new OverdueCheckouts(new HashMap<>()),
                noHolds());
    }

    static PatronInformation patronInformation(PatronId id, PatronType type) {
        return new PatronInformation(id, type);
    }

    public static Patron regularPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new Patron(
                patronInformation(patronId, Regular),
                List.of(regularPatronMaximumNumberOfHoldsPolicy),
                new OverdueCheckouts(new HashMap<>()),
                booksOnHold(numberOfHolds));
    }

    static Patron regularPatronWith(Hold hold) {
        PatronId patronId = anyPatronId();
        PatronHolds patronHolds = new PatronHolds(Collections.singleton(hold));
        return new Patron(
                patronInformation(patronId, Regular),
                allCurrentPolicies(),
                new OverdueCheckouts(new HashMap<>()),
                patronHolds);
    }

    public static Patron regularPatronWith(BookOnHold bookOnHold, PatronId patronId) {
        PatronHolds patronHolds = new PatronHolds(Collections.singleton(new Hold(bookOnHold.getBookId(), bookOnHold.getHoldPlacedAt())));
        return new Patron(
                patronInformation(patronId, Regular),
                allCurrentPolicies(),
                new OverdueCheckouts(new HashMap<>()),
                patronHolds);
    }

    public static Hold onHold() {
        return new Hold(anyBookId(), anyBranch());
    }

    static PatronHolds booksOnHold(int numberOfHolds) {
        return new PatronHolds(rangeClosed(1, numberOfHolds)
                .mapToObj(i -> new Hold(anyBookId(), anyBranch()))
                .collect(toSet()));
    }

    static Patron researcherPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new Patron(
                patronInformation(patronId, Researcher),
                List.of(regularPatronMaximumNumberOfHoldsPolicy),
                new OverdueCheckouts(new HashMap<>()),
                booksOnHold(numberOfHolds));
    }

    static Patron regularPatronWithOverdueCheckouts(LibraryBranchId libraryBranchId, Set<BookId> overdueBooks) {
        Map<LibraryBranchId, Set<BookId>> overdueCheckouts = new HashMap<>();
        overdueCheckouts.put(libraryBranchId, overdueBooks);
        return new Patron(
                patronInformation(anyPatronId(), Regular),
                List.of(overdueCheckoutsRejectionPolicy),
                new OverdueCheckouts(overdueCheckouts),
                noHolds());
    }

    static Patron regularPatronWith3_OverdueCheckoutsAt(LibraryBranchId libraryBranchId) {
        Map<LibraryBranchId, Set<BookId>> overdueCheckouts = new HashMap<>();
        overdueCheckouts.put(libraryBranchId, Set.of(anyBookId(), anyBookId(), anyBookId()));
        return new Patron(
                patronInformation(anyPatronId(), Regular),
                List.of(overdueCheckoutsRejectionPolicy),
                new OverdueCheckouts(overdueCheckouts),
                noHolds());
    }

    public static PatronId anyPatronId() {
        return patronId(UUID.randomUUID());
    }

    public static PatronId anyPatron() {
        return patronId(UUID.randomUUID());
    }

    static PatronId patronId(UUID patronId) {
        return new PatronId(patronId);
    }

    static PatronHolds noHolds() {
        return new PatronHolds(new HashSet<>());
    }


    public static Patron regularPatronWithHold(BookOnHold bookOnHold) {
        return regularPatronWith(new Hold(bookOnHold.getBookId(), bookOnHold.getHoldPlacedAt()));
    }


}
