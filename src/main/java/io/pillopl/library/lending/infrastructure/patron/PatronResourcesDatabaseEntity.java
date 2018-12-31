package io.pillopl.library.lending.infrastructure.patron;


import io.pillopl.library.lending.domain.patron.PatronInformation;
import io.pillopl.library.lending.domain.patron.PatronInformation.PatronType;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvent;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvent.ResourceCollected;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvent.ResourcePlacedOnHold;
import io.vavr.API;
import io.vavr.Predicates;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static io.vavr.API.$;
import static io.vavr.API.Case;

@NoArgsConstructor
class PatronResourcesDatabaseEntity {

    @Id
    Long id;
    UUID patronId;
    PatronType patronType;
    Set<ResourceOnHoldDatabaseEntity> resourcesOnHold;

    PatronResourcesDatabaseEntity(PatronInformation patronInformation) {
        this.patronId = patronInformation.getPatronId().getPatronId();
        this.patronType = patronInformation.getType();
        this.resourcesOnHold = new HashSet<>();
    }

    PatronResourcesDatabaseEntity reactTo(PatronResourcesEvent event) {
        return API.Match(event).of(
                Case($(Predicates.instanceOf(ResourcePlacedOnHold.class)), this::handle),
                Case($(Predicates.instanceOf(ResourceCollected.class)), this::handle)

        );
    }

    private PatronResourcesDatabaseEntity handle(ResourcePlacedOnHold event) {
        resourcesOnHold.add(new ResourceOnHoldDatabaseEntity(event.getResourceId(), event.getPatronId(), event.getLibraryBranchId()));
        return this;
    }

    private PatronResourcesDatabaseEntity handle(ResourceCollected event) {
        resourcesOnHold
                .stream()
                .filter(entity -> entity.hasSamePropertiesAs(event))
                .findAny()
                .ifPresent(entity -> resourcesOnHold.remove(entity));
        return this;
    }

}


@NoArgsConstructor
@EqualsAndHashCode
class ResourceOnHoldDatabaseEntity {
    @Id
    Long id;
    UUID patronId;
    UUID resourceId;
    UUID libraryBranchId;

    ResourceOnHoldDatabaseEntity(UUID resourceId, UUID patronId, UUID libraryBranchId) {
        this.resourceId = resourceId;
        this.patronId = patronId;
        this.libraryBranchId = libraryBranchId;
    }

    boolean hasSamePropertiesAs(ResourceCollected event) {
        return  this.patronId.equals(event.getPatronId()) &&
                this.resourceId.equals(event.getResourceId()) &&
                this.libraryBranchId.equals(event.getLibraryBranchId());
    }


}

class OverdueCheckoutDatabaseEntity {
    @Id
    Long id;
    UUID resourceId;
    UUID libraryBranchId;

}