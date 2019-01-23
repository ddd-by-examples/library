package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.model.BookId;
import io.pillopl.library.lending.library.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.*;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.PatronCreated;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
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

@AllArgsConstructor
class PatronBooksDatabaseRepository implements PatronBooksRepository {

    private final PatronBooksEntityRepository patronBooksEntityRepository;
    private final DomainModelMapper domainModelMapper;
    private final DomainEvents domainEvents;

    @Override
    public Option<PatronBooks> findBy(PatronId patronId) {
        return Option.of(patronBooksEntityRepository
                .findByPatronId(patronId.getPatronId()))
                .map(domainModelMapper::map);
    }

    @Override
    public PatronBooks publish(PatronBooksEvent domainEvent) {
        PatronBooks result = Match(domainEvent).of(
                Case($(instanceOf(PatronCreated.class)), this::createNewPatron),
                Case($(), this::handleNextEvent));
        domainEvents.publish(domainEvent.normalize());
        return result;
    }

    private PatronBooks createNewPatron(PatronCreated domainEvent) {
        PatronBooksDatabaseEntity entity = patronBooksEntityRepository
                .save(new PatronBooksDatabaseEntity(domainEvent.patronId(), domainEvent.getPatronType()));
        return domainModelMapper.map(entity);
    }

    private PatronBooks handleNextEvent(PatronBooksEvent domainEvent) {
        PatronBooksDatabaseEntity entity = patronBooksEntityRepository.findByPatronId(domainEvent.patronId().getPatronId());
        entity = entity.handle(domainEvent);
        entity = patronBooksEntityRepository.save(entity);
        return domainModelMapper.map(entity);
    }

}

interface PatronBooksEntityRepository extends CrudRepository<PatronBooksDatabaseEntity, Long> {

    @Query("SELECT p.* FROM patron_books_database_entity p where p.patron_id = :patronId")
    PatronBooksDatabaseEntity findByPatronId(@Param("patronId") UUID patronId);

}

@AllArgsConstructor
class DomainModelMapper {

    private final PatronBooksFactory patronBooksFactory;

    PatronBooks map(PatronBooksDatabaseEntity entity) {
        return patronBooksFactory.create(
                entity.patronType,
                new PatronId(entity.patronId),
                mapPatronHolds(entity),
                mapPatronOverdueCheckouts(entity)
        );
    }

    Map<LibraryBranchId, Set<BookId>> mapPatronOverdueCheckouts(PatronBooksDatabaseEntity patronBooksDatabaseEntity) {
        return
                patronBooksDatabaseEntity
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

    Set<Tuple2<BookId, LibraryBranchId>> mapPatronHolds(PatronBooksDatabaseEntity patronBooksDatabaseEntity) {
        return patronBooksDatabaseEntity
                .booksOnHold
                .stream()
                .map(entity -> Tuple.of((new BookId(entity.bookId)), new LibraryBranchId(entity.libraryBranchId)))
                .collect(toSet());
    }

}
