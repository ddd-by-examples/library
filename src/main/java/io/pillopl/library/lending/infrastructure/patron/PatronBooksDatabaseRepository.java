package io.pillopl.library.lending.infrastructure.patron;

import io.pillopl.library.lending.application.expiredhold.FindExpiredHolds;
import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.*;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.PatronCreated;
import io.vavr.Tuple;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.collection.List.ofAll;
import static java.util.stream.Collectors.*;

class PatronBooksDatabaseRepository implements PatronBooksRepository, FindExpiredHolds {

    private final PatronBooksEntityRepository patronBooksEntityRepository;
    private final PatronBooksFactory patronBooksFactory;
    private final DomainModelMapper domainModelMapper;
    private final Clock clock;

    PatronBooksDatabaseRepository(PatronBooksEntityRepository patronBooksEntityRepository,
                                  PatronBooksFactory patronBooksFactory, DomainModelMapper domainModelMapper, Clock clock) {
        this.patronBooksEntityRepository = patronBooksEntityRepository;
        this.patronBooksFactory = patronBooksFactory;
        this.domainModelMapper = domainModelMapper;
        this.clock = clock;
    }

    @Override
    public Option<PatronBooks> findBy(PatronId patronId) {
        return Option.of(patronBooksEntityRepository
                .findByPatronId(patronId.getPatronId()))
                .map(this::mapDataModelToDomainModel);
    }

    private PatronBooks mapDataModelToDomainModel(PatronBooksDatabaseEntity patronBooksDatabaseEntity) {
        return patronBooksFactory.recreateFrom(
                domainModelMapper.mapPatronInformation(patronBooksDatabaseEntity),
                domainModelMapper.mapPatronHolds(patronBooksDatabaseEntity),
                domainModelMapper.mapPatronOverdueCheckouts(patronBooksDatabaseEntity));
    }

    @Override
    public Try<PatronBooks> handle(PatronBooksEvent domainEvent) {
        return Try.of(() -> Match(domainEvent).of(
                Case($(instanceOf(PatronCreated.class)), this::createNewPatron),
                Case($(), this::handleNextEvent)
        ));
    }

    private PatronBooks createNewPatron(PatronCreated domainEvent) {
        PatronBooksDatabaseEntity entity = patronBooksEntityRepository
                .save(new PatronBooksDatabaseEntity(new PatronInformation(domainEvent.patronId(), domainEvent.getPatronType())));
        return mapDataModelToDomainModel(entity);
    }

    private PatronBooks handleNextEvent(PatronBooksEvent domainEvent) {
        PatronBooksDatabaseEntity dataModel = findDataModelFor(domainEvent.patronId());
        dataModel = dataModel.handle(domainEvent);
        dataModel = patronBooksEntityRepository.save(dataModel);
        return mapDataModelToDomainModel(dataModel);
    }

    private PatronBooksDatabaseEntity findDataModelFor(PatronId patronId) {
        return patronBooksEntityRepository.findByPatronId(patronId.getPatronId());
    }

    @Override
    public ExpiredHolds allExpiredHolds() {
        return new ExpiredHolds(ofAll(
                patronBooksEntityRepository.findHoldsExpiredAt(Instant.now(clock))
                .stream()
                .map(entity -> Tuple.of(
                        new BookId(entity.bookId),
                        new PatronId(entity.patronId),
                        new LibraryBranchId(entity.libraryBranchId)))
                .collect(toList())));
    }
}

interface PatronBooksEntityRepository extends CrudRepository<PatronBooksDatabaseEntity, Long> {


    @Query("SELECT p.* FROM patron_books_database_entity p where p.patron_id = :patronId")
    PatronBooksDatabaseEntity findByPatronId(@Param("patronId") UUID patronId);

    @Query("SELECT b.* FROM book_on_hold_database_entity b where b.till > :at")
    List<BookOnHoldDatabaseEntity> findHoldsExpiredAt(@Param("at") Instant at);

}

class DomainModelMapper {

    OverdueCheckouts mapPatronOverdueCheckouts(PatronBooksDatabaseEntity patronBooksDatabaseEntity) {
        return new OverdueCheckouts(
                patronBooksDatabaseEntity
                .overdueCheckouts
                .stream()
                .collect(groupingBy(OverdueCheckoutDatabaseEntity::getLibraryBranchId, toSet()))
                .entrySet()
                .stream()
                .collect(toMap(
                        (Entry<UUID,Set<OverdueCheckoutDatabaseEntity>> entry) -> new LibraryBranchId(entry.getKey()),
                        entry -> entry
                                .getValue()
                                .stream()
                                .map(entity -> new OverdueCheckout(new BookId(entity.bookId)))
                                .collect(toSet()))));
    }

    PatronHolds mapPatronHolds(PatronBooksDatabaseEntity patronBooksDatabaseEntity) {
        return new PatronHolds(patronBooksDatabaseEntity
                .booksOnHold
                .stream()
                .map(entity -> new PatronHold(new BookId(entity.bookId), new LibraryBranchId(entity.libraryBranchId)))
                .collect(toSet()));
    }

    PatronInformation mapPatronInformation(PatronBooksDatabaseEntity patronBooksDatabaseEntity) {
        return new PatronInformation(new PatronId(patronBooksDatabaseEntity.patronId), patronBooksDatabaseEntity.patronType);
    }
}
