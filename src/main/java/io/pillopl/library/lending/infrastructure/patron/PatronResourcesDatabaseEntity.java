package io.pillopl.library.lending.infrastructure.patron;


import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.pillopl.library.lending.domain.patron.PatronInformation;
import io.pillopl.library.lending.domain.patron.PatronInformation.PatronType;
import io.pillopl.library.lending.domain.patron.PatronResourcesSnapshot;
import io.pillopl.library.lending.domain.patron.ResourceOnHoldSnapshot;
import io.pillopl.library.lending.domain.resource.ResourceId;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toSet;

@NoArgsConstructor
class PatronResourcesDatabaseEntity {

    @Id Long id;
    UUID patronId;
    PatronType patronType;
    Set<ResourceOnHoldDatabaseEntity> resourcesOnHold;


    PatronResourcesDatabaseEntity(PatronInformation patronInformation) {
        this.patronId = patronInformation.getPatronId().getPatronId();
        this.patronType = patronInformation.getType();
        this.resourcesOnHold = new HashSet<>();
    }

    void synchronizeWith(PatronResourcesSnapshot snapshot) {
        this.recreateResourcesOnHoldFrom(snapshot);
    }

    void recreateResourcesOnHoldFrom(PatronResourcesSnapshot snapshot) {
        resourcesOnHold =
                snapshot
                .getResourcesOnHold()
                .stream()
                .map(resourceOnHoldSnapshot -> new ResourceOnHoldDatabaseEntity(resourceOnHoldSnapshot, snapshot.getPatronInformation().getPatronId()))
                .collect(toSet());
    }

    void recreateOverdueCheckoutsFrom(PatronResourcesSnapshot snapshot) {

    }

    PatronResourcesSnapshot toSnapshot() {
        //TODO add checkouts
        Set<ResourceOnHoldSnapshot> snapshot =
                resourcesOnHold
                .stream()
                .map(resourceOnHoldDatabaseEntity ->
                        new ResourceOnHoldSnapshot(new ResourceId(resourceOnHoldDatabaseEntity.resourceId), new LibraryBranchId(resourceOnHoldDatabaseEntity.libraryBranchId)))
                .collect(toSet());
        return new PatronResourcesSnapshot(new PatronInformation(new PatronId(patronId), patronType), snapshot, Collections.emptyMap());
    }
}

@NoArgsConstructor
class ResourceOnHoldDatabaseEntity {
    @Id Long id;
    UUID patronId;
    UUID resourceId;
    UUID libraryBranchId;

    ResourceOnHoldDatabaseEntity(ResourceOnHoldSnapshot resourceOnHoldSnapshot, PatronId patronId) {
        this.patronId = patronId.getPatronId();
        this.resourceId = resourceOnHoldSnapshot.getResourceId().getResourceId();
        this.libraryBranchId = resourceOnHoldSnapshot.getLibraryBranchId().getLibraryBranchId();
    }

}

class OverdueCheckoutDatabaseEntity {
    @Id Long id;
    UUID resourceId;
    UUID libraryBranchId;

}