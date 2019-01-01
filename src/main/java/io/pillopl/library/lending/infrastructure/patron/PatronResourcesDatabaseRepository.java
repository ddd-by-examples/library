package io.pillopl.library.lending.infrastructure.patron;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.*;
import io.pillopl.library.lending.domain.resource.ResourceId;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.REGULAR;
import static java.util.stream.Collectors.toSet;

class PatronResourcesDatabaseRepository implements PatronResourcesRepository {

    private final PatronResourcesEntityRepository patronResourcesEntityRepository;
    private final PatronResourcesFactory patronResourcesFactory;

    PatronResourcesDatabaseRepository(PatronResourcesEntityRepository patronResourcesEntityRepository,
                                      PatronResourcesFactory patronResourcesFactory) {
        this.patronResourcesEntityRepository = patronResourcesEntityRepository;
        this.patronResourcesFactory = patronResourcesFactory;
    }

    @Override
    public Option<PatronResources> findBy(PatronId patronId) {
        return patronResourcesEntityRepository
                .findByPatronId(patronId.getPatronId())
                .map(this::recreateDomainModelFromDataModel);
    }

    private PatronResources recreateDomainModelFromDataModel(PatronResourcesDatabaseEntity patronResourceEntity) {
        PatronInformation patronInformation =
                new PatronInformation(new PatronId(patronResourceEntity.patronId), patronResourceEntity.patronType);
        ResourcesOnHold resourcesOnHold =
                new ResourcesOnHold(patronResourceEntity
                        .resourcesOnHold
                        .stream()
                        .map(entity -> new ResourceOnHold(new ResourceId(entity.resourceId), new LibraryBranchId(entity.libraryBranchId)))
                        .collect(toSet()));
        return patronResourcesFactory.recreateFrom(patronInformation, resourcesOnHold);
    }

    @Override
    public Try<Void> reactTo(PatronResourcesEvent domainEvent) {
        //TODO add optimistic locking
        return Try.run(() -> {
            PatronResourcesDatabaseEntity dataModel = findOrCreateNewFor(domainEvent.patronId());
            dataModel.reactTo(domainEvent);
            patronResourcesEntityRepository.save(dataModel);
        });
    }

    private PatronResourcesDatabaseEntity findOrCreateNewFor(PatronId patronId) {
        //TODO change regular
        return patronResourcesEntityRepository.findByPatronId(patronId.getPatronId())
                .getOrElse(() -> new PatronResourcesDatabaseEntity(new PatronInformation(patronId, REGULAR)));
    }

}

interface PatronResourcesEntityRepository extends CrudRepository<PatronResourcesDatabaseEntity, Long> {

    @Query("SELECT p.* FROM patron_resources_database_entity p where p.patron_id = :patronId")
    Option<PatronResourcesDatabaseEntity> findByPatronId(@Param("patronId") UUID patronId);

}
