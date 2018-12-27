package io.pillopl.books.domain;

import lombok.Value;

import java.time.Instant;
import java.util.UUID;

class PatronResourcesEvents {

    @Value
    static class ResourceHeld {
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
    static class ResourceHoldRequestFailed {
        String reason;
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;
    }

    @Value
    static class ResourceCollectingFailed {
        String reason;
        Instant when;
        UUID patronId;
        UUID resourceId;
        UUID libraryBranchId;
    }
}



