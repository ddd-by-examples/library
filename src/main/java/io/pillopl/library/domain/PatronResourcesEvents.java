package io.pillopl.library.domain;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

//TODO add notNull
class PatronResourcesEvents {

    @Value
    static class ResourcePlacedOnHold {
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;
    }

    @Value
    static class ResourceCollected {
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;
    }

    @Value
    static class ResourceReturned {
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;
    }

    @Value
    static class ResourceHoldFailed {
        String reason;
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;

        static ResourceHoldFailed now(String reason, Resource resource, PatronInformation patronInformation) {
            return new ResourceHoldFailed(
                    reason,
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    resource.getResourceId().getResourceId(),
                    resource.getLibraryBranch().getLibraryBranchId());
        }
    }

    @Value
    static class ResourceCollectingFailed {
        String reason;
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;

        static ResourceCollectingFailed now(String reason, Resource resource, PatronInformation patronInformation) {
            return new ResourceCollectingFailed(
                    reason,
                    Instant.now(),
                    patronInformation.getPatronId().getPatronId(),
                    resource.getResourceId().getResourceId(),
                    resource.getLibraryBranch().getLibraryBranchId());
        }
    }
}



