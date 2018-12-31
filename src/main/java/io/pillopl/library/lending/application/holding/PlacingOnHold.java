package io.pillopl.library.lending.application.holding;

import io.pillopl.library.lending.domain.patron.PatronId;
import io.pillopl.library.lending.domain.patron.PatronResources;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvents.ResourceHoldFailed;
import io.pillopl.library.lending.domain.patron.PatronResourcesEvents.ResourcePlacedOnHold;
import io.pillopl.library.lending.domain.patron.PatronResourcesRepository;
import io.pillopl.library.lending.domain.resource.Resource;
import io.pillopl.library.lending.domain.resource.ResourceId;
import io.vavr.control.Either;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static io.pillopl.library.lending.application.holding.PlacingOnHold.Result.FAILURE;
import static io.pillopl.library.lending.application.holding.PlacingOnHold.Result.SUCCESS;
import static io.vavr.API.*;
import static io.vavr.Patterns.$Left;
import static io.vavr.Patterns.$Right;

@AllArgsConstructor
public class PlacingOnHold {

    enum Result {
        SUCCESS, FAILURE
    }

    private final FindResource findResource;
    private final PatronResourcesRepository patronResourcesRepository;

    @Transactional
    public Result placeOnHold(ResourceId id, PatronId patronId) {
        Resource resource = findResource(id);
        PatronResources patronResources = findPatronCurrentResources(patronId);
        Either<ResourceHoldFailed, ResourcePlacedOnHold> result = patronResources.placeOnHold(resource);
        savePatronResources(patronResources);
        return Match(result).of(
                Case($Left($()), this::publish),
                Case($Right($()), this::publish)
        );

    }

    private void savePatronResources(PatronResources patronResources) {
        patronResourcesRepository.save(patronResources);
    }

    private Result publish(ResourceHoldFailed resourceHoldFailed) {
        //TODO publish events
        return FAILURE;
    }

    private Result publish(ResourcePlacedOnHold placedOnHold) {
        //TODO publish events
        return SUCCESS;
    }

    private Resource findResource(ResourceId id) {
        return findResource.with(id)
                .getOrElseThrow(() -> new IllegalArgumentException("Resource with given Id does not exists: " + id.getResourceId()));
    }

    private PatronResources findPatronCurrentResources(PatronId patronId) {
        return patronResourcesRepository
                .findBy(patronId)
                .getOrElseThrow(() -> new IllegalArgumentException("Patron with given Id does not exists: " + patronId.getPatronId()));
    }
}
