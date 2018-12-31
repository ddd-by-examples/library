package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.resource.ResourceId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

//TODO add notNull
//TODO add id to events
public interface PatronResourcesEvent {

    default PatronId patronId() {
        return new PatronId(getPatronId());
    }

    UUID getPatronId();

    @Value
    class ResourcePlacedOnHold implements PatronResourcesEvent {
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;

        static ResourcePlacedOnHold now(ResourceId resourceId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new ResourcePlacedOnHold(
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    resourceId.getResourceId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class ResourceCollected implements PatronResourcesEvent {
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;

        static ResourceCollected now(ResourceId resourceId, LibraryBranchId libraryBranchId, PatronId patronId) {
            return new ResourceCollected(
                    Instant.now(),
                    patronId.getPatronId(),
                    resourceId.getResourceId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class ResourceReturned implements PatronResourcesEvent {
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;
    }

    @Value
    class ResourceHoldFailed implements PatronResourcesEvent {
        String reason;
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;

        static ResourceHoldFailed now(Reason reason, ResourceId resourceId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new ResourceHoldFailed(
                    reason.getReason(),
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    resourceId.getResourceId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    class ResourceCollectingFailed implements PatronResourcesEvent {
        String reason;
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;

        static ResourceCollectingFailed now(Reason reason, ResourceId resourceId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new ResourceCollectingFailed(
                    reason.getReason(),
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    resourceId.getResourceId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }
}



