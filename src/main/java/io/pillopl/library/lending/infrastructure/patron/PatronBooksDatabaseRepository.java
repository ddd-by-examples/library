package io.pillopl.library.lending.infrastructure.patron;

import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.*;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.PatronCreated;
import io.vavr.Predicates;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import static io.vavr.API.*;
import static java.util.stream.Collectors.*;

class PatronBooksDatabaseRepository implements PatronBooksRepository {

    private final PatronBooksEntityRepository patronBooksEntityRepository;
    private final PatronBooksFactory patronBooksFactory;
    private final DomainModelMapper domainModelMapper;

    PatronBooksDatabaseRepository(PatronBooksEntityRepository patronBooksEntityRepository,
                                  PatronBooksFactory patronBooksFactory, DomainModelMapper domainModelMapper) {
        this.patronBooksEntityRepository = patronBooksEntityRepository;
        this.patronBooksFactory = patronBooksFactory;
        this.domainModelMapper = domainModelMapper;
    }

    @Override
    public Option<PatronBooks> findBy(PatronId patronId) {
        return patronBooksEntityRepository
                .findByPatronId(patronId.getPatronId())
                .map(this::mapDataModelToDomainModel);
    }

    private PatronBooks mapDataModelToDomainModel(PatronBooksDatabaseEntity patronBooksDatabaseEntity) {
        return patronBooksFactory.recreateFrom(
                domainModelMapper.mapPatronInformation(patronBooksDatabaseEntity),
                domainModelMapper.mapPatronHolds(patronBooksDatabaseEntity),
                domainModelMapper.mapPatronOverdueCheckouts(patronBooksDatabaseEntity));
    }

    @Override
    public Try<Void> reactTo(PatronBooksEvent domainEvent) {
        return Try.run(() -> {
            Match(domainEvent).of(
                    Case($(Predicates.instanceOf(PatronCreated.class)), this::createNewPatron),
                    Case($(), this::handleNextEvent)
            );
        });
    }

    private Option<PatronBooksDatabaseEntity> createNewPatron(PatronCreated domainEvent) {
        return Option.of(
                patronBooksEntityRepository
                .save(new PatronBooksDatabaseEntity(new PatronInformation(domainEvent.patronId(), domainEvent.getPatronType()))));
    }

    private Option<PatronBooksDatabaseEntity> handleNextEvent(PatronBooksEvent domainEvent) {
        return findDataModelFor(domainEvent.patronId())
                .map(entity -> entity.reactTo(domainEvent))
                .map(patronBooksEntityRepository::save);
    }

    private Option<PatronBooksDatabaseEntity> findDataModelFor(PatronId patronId) {
        return patronBooksEntityRepository.findByPatronId(patronId.getPatronId());
    }

}

interface PatronBooksEntityRepository extends CrudRepository<PatronBooksDatabaseEntity, Long> {

    @Query("SELECT p.* FROM patron_books_database_entity p where p.patron_id = :patronId")
    Option<PatronBooksDatabaseEntity> findByPatronId(@Param("patronId") UUID patronId);

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
