package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.library.LibraryBranchFixture;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.resource.ResourceFixture;
import io.pillopl.library.lending.domain.resource.ResourceId;
import io.vavr.collection.List;

import java.util.*;
import java.util.stream.Collectors;

import static io.pillopl.library.lending.domain.patron.PlacingOnHoldPolicy.allCurrentPolicies;
import static java.util.stream.IntStream.rangeClosed;

public class PatronResourcesFixture {

    public static PatronResources regularPatron() {
        return regularPatron(anyPatronId());
    }

    public static PatronResources regularPatron(PatronId patronId) {
        return new PatronResources(
                patronInformation(patronId, PatronInformation.PatronType.REGULAR),
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
                .mapToObj(i -> new ResourceOnHold(ResourceFixture.anyResourceId(), LibraryBranchFixture.anyBranch()))
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

    static PatronResources regularPatronWithOverdueCheckouts(LibraryBranchId libraryBranchId, Set<ResourceId> overdueResources) {
        Map<LibraryBranchId, Set<ResourceId>> overdueCheckouts = new HashMap<>();
        overdueCheckouts.put(libraryBranchId, overdueResources);
        return new PatronResources(
                patronInformation(anyPatronId(), PatronInformation.PatronType.REGULAR),
                allCommonPlacingOnHoldPolicies(),
                new OverdueCheckouts(overdueCheckouts),
                noHolds());
    }

    public static PatronId anyPatronId() {
        return patronId(UUID.randomUUID());
    }


    static PatronId patronId(UUID patronId) {
        return new PatronId(patronId);
    }

    static ResourcesOnHold noHolds() {
        return new ResourcesOnHold(new HashSet<>());
    }

    static List<PlacingOnHoldPolicy> allCommonPlacingOnHoldPolicies() {
        return allCurrentPolicies();
    }

}
