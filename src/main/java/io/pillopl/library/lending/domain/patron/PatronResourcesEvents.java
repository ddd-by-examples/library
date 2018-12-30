package io.pillopl.library.lending.domain.patron;

import io.pillopl.library.lending.domain.resource.ResourceId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

//TODO add notNull
public class PatronResourcesEvents {

    @Value
    public static class ResourcePlacedOnHold {
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
    public static class ResourceCollected {
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
    public static class ResourceReturned {
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;
    }

    @Value
    public static class ResourceHoldFailed {
        String reason;
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;

        static ResourceHoldFailed now(String reason, ResourceId resourceId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new ResourceHoldFailed(
                    reason,
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    resourceId.getResourceId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }

    @Value
    public static class ResourceCollectingFailed {
        String reason;
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;

        static ResourceCollectingFailed now(String reason, ResourceId resourceId, LibraryBranchId libraryBranchId, PatronInformation patronInformation) {
            return new ResourceCollectingFailed(
                    reason,
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    resourceId.getResourceId(),
                    libraryBranchId.getLibraryBranchId());
        }
    }
}



