package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.*;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static java.util.stream.Collectors.*;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class PatronsDatabaseRepository implements Patrons {

    private final PatronEntityRepository patronEntityRepository;
    private final DomainModelMapper domainModelMapper;
    private final DomainEvents domainEvents;

    @Override
    public Option<Patron> findBy(PatronId patronId) {
        return Option.of(patronEntityRepository
                .findByPatronId(patronId.getPatronId()))
                .map(domainModelMapper::map);
    }

    @Override
    public Patron publish(PatronEvent domainEvent) {
        Patron result = Match(domainEvent).of(
                Case($(instanceOf(PatronCreated.class)), this::createNewPatron),
                Case($(), this::handleNextEvent));
        domainEvents.publish(domainEvent.normalize());
        return result;
    }

    private Patron createNewPatron(PatronCreated domainEvent) {
        PatronDatabaseEntity entity = patronEntityRepository
                .save(new PatronDatabaseEntity(domainEvent.patronId(), domainEvent.getPatronType()));
        return domainModelMapper.map(entity);
    }

    private Patron handleNextEvent(PatronEvent domainEvent) {
        PatronDatabaseEntity entity = patronEntityRepository.findByPatronId(domainEvent.patronId().getPatronId());
        entity = entity.handle(domainEvent);
        entity = patronEntityRepository.save(entity);
        return domainModelMapper.map(entity);
    }

}

interface PatronEntityRepository extends CrudRepository<PatronDatabaseEntity, Long> {

    @Query("SELECT p.* FROM patron_database_entity p where p.patron_id = :patronId")
    PatronDatabaseEntity findByPatronId(@Param("patronId") UUID patronId);

}

@AllArgsConstructor
class DomainModelMapper {

    private final PatronFactory patronFactory;

    Patron map(PatronDatabaseEntity entity) {
        return patronFactory.create(
                entity.patronType,
                new PatronId(entity.patronId),
                mapPatronHolds(entity),
                mapPatronOverdueCheckouts(entity)
        );
    }

    Map<LibraryBranchId, Set<BookId>> mapPatronOverdueCheckouts(PatronDatabaseEntity patronDatabaseEntity) {
        return
                patronDatabaseEntity
                        .checkouts
                        .stream()
                        .collect(groupingBy(OverdueCheckoutDatabaseEntity::getLibraryBranchId, toSet()))
                        .entrySet()
                        .stream()
                        .collect(toMap(
                                (Entry<UUID, Set<OverdueCheckoutDatabaseEntity>> entry) -> new LibraryBranchId(entry.getKey()), entry -> entry
                                        .getValue()
                                        .stream()
                                        .map(entity -> (new BookId(entity.bookId)))
                                        .collect(toSet())));
    }

    Set<Tuple2<BookId, LibraryBranchId>> mapPatronHolds(PatronDatabaseEntity patronDatabaseEntity) {
        return patronDatabaseEntity
                .booksOnHold
                .stream()
                .map(entity -> Tuple.of((new BookId(entity.bookId)), new LibraryBranchId(entity.libraryBranchId)))
                .collect(toSet());
    }

}
