package io.pillopl.books.domain;

import io.pillopl.books.domain.PatronInformation.PatronType;
import io.vavr.collection.List;

import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.pillopl.books.domain.LibraryBranchFixture.anyBranch;
import static io.pillopl.books.domain.OverdueCheckouts.noOverdueCheckouts;
import static io.pillopl.books.domain.PatronInformation.PatronType.REGULAR;
import static io.pillopl.books.domain.PatronInformation.PatronType.RESEARCHER;
import static io.vavr.collection.List.of;
import static java.util.stream.IntStream.rangeClosed;

class PatronResourcesFixture {

    static PatronResources regularPatron() {
        return new PatronResources(
                patronInformation(anyPatronId(), REGULAR),
                allCommonPlacingOnHoldPolicies(),
                noOverdueCheckouts(),
                noHolds());
    }

    static PatronInformation patronInformation(PatronId id, PatronType type) {
        return new PatronInformation(id, type);
    }

    static PatronResources researcherPatron() {
        return new PatronResources(
                patronInformation(anyPatronId(), RESEARCHER),
                allCommonPlacingOnHoldPolicies(),
                noOverdueCheckouts(),
                noHolds());
    }

    static PatronResources regularPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronResources(
                patronInformation(patronId, REGULAR),
                allCommonPlacingOnHoldPolicies(),
                noOverdueCheckouts(),
                resourcesOnHold(numberOfHolds, patronId));
    }

    static ResourcesOnHold resourcesOnHold(int numberOfHolds, PatronId patronId) {
        return new ResourcesOnHold(rangeClosed(1, numberOfHolds)
                .mapToObj(i -> new ResourceOnHold(patronId, ResourceFixture.anyResourceId(), anyBranch()))
                .collect(Collectors.toSet()));
    }

    static PatronResources researcherPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronResources(
                patronInformation(patronId, RESEARCHER),
                allCommonPlacingOnHoldPolicies(),
                noOverdueCheckouts(),
                resourcesOnHold(numberOfHolds, patronId));
    }

    static PatronResources regularPatronWithOverdueCheckouts(OverdueCheckouts overdueCheckouts) {
        return new PatronResources(
                patronInformation(anyPatronId(), REGULAR),
                allCommonPlacingOnHoldPolicies(),
                overdueCheckouts,
                noHolds());
    }

    static PatronResources researcherPatronWithOverdueCheckouts(OverdueCheckouts overdueCheckouts) {
        return new PatronResources(
                patronInformation(anyPatronId(), RESEARCHER),
                allCommonPlacingOnHoldPolicies(),
                overdueCheckouts,
                noHolds());
    }

    static PatronId anyPatronId() {
        return patronId(UUID.randomUUID());
    }


    static PatronId patronId(UUID patronId) {
        return new PatronId(patronId);
    }

    static ResourcesOnHold noHolds() {
        return new ResourcesOnHold(new HashSet<>());
    }

    static List<PlacingOnHoldPolicy> allCommonPlacingOnHoldPolicies() {
        return of(
                new OverdueCheckoutsRejectionPolicy(),
                new RegularPatronMaximumNumberOfHoldsPolicy(),
                new OnlyResearcherPatronsCanBookRestrictedResourcePolicy());
    }

}
