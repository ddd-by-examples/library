package io.pillopl.library.lending.infrastructure.patron;

import io.pillopl.library.lending.domain.patron.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
class PatronResourcesDatabaseRepository implements PatronResourcesRepository {

    private final PatronResourcesEntityRepository patronResourcesEntityRepository;
    private final PatronResourcesFactory patronResourcesFactory;

    PatronResourcesDatabaseRepository(PatronResourcesEntityRepository patronResourcesEntityRepository, PatronResourcesFactory patronResourcesFactory) {
        this.patronResourcesEntityRepository = patronResourcesEntityRepository;
        this.patronResourcesFactory = patronResourcesFactory;
    }

    @Override
    public Option<PatronResources> findBy(PatronId patronId) {
        return patronResourcesEntityRepository.findByPatronId(patronId.getPatronId())
                .map(patronResourceEntity -> patronResourcesFactory.recreateFrom(patronResourceEntity.toSnapshot()));
    }

    @Override
    @Transactional
    public Try<PatronResources> save(PatronResources patronResources) {
        //TODO add optimistic locking
        return Try.ofSupplier(() -> {
            PatronResourcesSnapshot snapshot = patronResources.toSnapshot();
            PatronResourcesDatabaseEntity dataModel = findOrCreateNewFor(snapshot);
            dataModel.synchronizeWith(snapshot);
            patronResourcesEntityRepository.save(dataModel);
            return patronResources;
        });
    }

    private PatronResourcesDatabaseEntity findOrCreateNewFor(PatronResourcesSnapshot snapshot) {
        return patronResourcesEntityRepository.findByPatronId(snapshot.getPatronInformation().getPatronId().getPatronId())
                .getOrElse(() -> new PatronResourcesDatabaseEntity(snapshot.getPatronInformation()));
    }


}

interface PatronResourcesEntityRepository extends CrudRepository<PatronResourcesDatabaseEntity, Long> {

    @Query("SELECT p.* FROM patron_resources_database_entity p where p.patron_id = :patronId")
    Option<PatronResourcesDatabaseEntity> findByPatronId(@Param("patronId") UUID patronId);

}
