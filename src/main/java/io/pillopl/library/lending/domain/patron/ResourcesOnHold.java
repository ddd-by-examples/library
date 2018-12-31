package io.pillopl.library.lending.domain.patron;

import lombok.Value;

import java.util.Set;

@Value
//TODO add not null
public class ResourcesOnHold {

    Set<ResourceOnHold> resourcesOnHold;

    boolean doesNotContain(ResourceOnHold resourceOnHold) {
        return !resourcesOnHold.contains(resourceOnHold);
    }

    int count() {
        return resourcesOnHold.size();
    }

}
