package io.pillopl.library.domain;

import io.vavr.collection.List;

import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.vavr.collection.List.of;
import static java.util.stream.IntStream.rangeClosed;

class PatronResourcesFixture {

    static PatronResources regularPatron() {
        return new PatronResources(
                patronInformation(anyPatronId(), PatronInformation.PatronType.REGULAR),
                allCommonPlacingOnHoldPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                noHolds());
    }

    static PatronInformation patronInformation(PatronId id, PatronInformation.PatronType type) {
        return new PatronInformation(id, type);
    }

    static PatronResources researcherPatron() {
        return new PatronResources(
                patronInformation(anyPatronId(), PatronInformation.PatronType.RESEARCHER),
                allCommonPlacingOnHoldPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                noHolds());
    }

    static PatronResources regularPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronResources(
                patronInformation(patronId, PatronInformation.PatronType.REGULAR),
                allCommonPlacingOnHoldPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                resourcesOnHold(numberOfHolds, patronId));
    }

    static ResourcesOnHold resourcesOnHold(int numberOfHolds, PatronId patronId) {
        return new ResourcesOnHold(rangeClosed(1, numberOfHolds)
                .mapToObj(i -> new ResourceOnHold(patronId, ResourceFixture.anyResourceId(), LibraryBranchFixture.anyBranch()))
                .collect(Collectors.toSet()));
    }

    static PatronResources researcherPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronResources(
                patronInformation(patronId, PatronInformation.PatronType.RESEARCHER),
                allCommonPlacingOnHoldPolicies(),
                OverdueCheckouts.noOverdueCheckouts(),
                resourcesOnHold(numberOfHolds, patronId));
    }

    static PatronResources regularPatronWithOverdueCheckouts(OverdueCheckouts overdueCheckouts) {
        return new PatronResources(
                patronInformation(anyPatronId(), PatronInformation.PatronType.REGULAR),
                allCommonPlacingOnHoldPolicies(),
                overdueCheckouts,
                noHolds());
    }

    static PatronResources researcherPatronWithOverdueCheckouts(OverdueCheckouts overdueCheckouts) {
        return new PatronResources(
                patronInformation(anyPatronId(), PatronInformation.PatronType.RESEARCHER),
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
