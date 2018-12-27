package io.pillopl.books.domain;

import io.pillopl.books.domain.PatronInformation.PatronType;

import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.pillopl.books.domain.LibraryBranchFixture.anyBranch;
import static io.pillopl.books.domain.PatronInformation.PatronType.REGULAR;
import static io.pillopl.books.domain.PatronInformation.PatronType.RESEARCHER;
import static java.util.stream.IntStream.rangeClosed;

class PatronResourcesFixture {

    static PatronResources regularPatron() {
        return new PatronResources(patronInformation(anyPatronId(), REGULAR), OverdueResources.noOverdueResources(), noHolds());
    }

    private static PatronInformation patronInformation(PatronId id, PatronType type) {
        return new PatronInformation(id, type);
    }

    static PatronResources researcherPatron() {
        return new PatronResources(patronInformation(anyPatronId(), RESEARCHER), OverdueResources.noOverdueResources(), noHolds());
    }

    static PatronResources regularPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronResources(patronInformation(patronId, REGULAR), OverdueResources.noOverdueResources(), resourcesOnHold(numberOfHolds, patronId));
    }

    static ResourcesOnHold resourcesOnHold(int numberOfHolds, PatronId patronId) {
        return new ResourcesOnHold(rangeClosed(1, numberOfHolds)
                .mapToObj(i -> new ResourceOnHold(patronId, ResourceFixture.anyResourceId(), anyBranch()))
                .collect(Collectors.toSet()));
    }

    static PatronResources researcherPatronWithHolds(int numberOfHolds) {
        PatronId patronId = anyPatronId();
        return new PatronResources(patronInformation(patronId, RESEARCHER), OverdueResources.noOverdueResources(), resourcesOnHold(numberOfHolds, patronId));
    }

    static PatronResources regularPatronWithOverdueResource(OverdueResources overdueResources) {
        return new PatronResources(patronInformation(anyPatronId(), REGULAR), overdueResources, noHolds());
    }

    static PatronResources researcherPatronWithOverdueResource(OverdueResources overdueResources) {
        return new PatronResources(patronInformation(anyPatronId(), RESEARCHER), overdueResources, noHolds());
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


}
