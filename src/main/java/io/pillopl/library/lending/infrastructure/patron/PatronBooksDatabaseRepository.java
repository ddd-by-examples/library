package io.pillopl.library.lending.infrastructure.patron;

import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.*;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.PatronCreated;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;

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

    @Override
    public Option<PatronBooks> findBy(PatronId patronId) {
        return Option.of(patronBooksEntityRepository
                .findByPatronId(patronId.getPatronId()))
                .map(domainModelMapper::map);
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
        return domainModelMapper.map(entity);
    }

    private PatronBooks handleNextEvent(PatronBooksEvent domainEvent) {
        PatronBooksDatabaseEntity entity = patronBooksEntityRepository.findByPatronId(domainEvent.patronId().getPatronId());
        entity = entity.handle(domainEvent);
        entity = patronBooksEntityRepository.save(entity);
        return domainModelMapper.map(entity);
    }

}

@AllArgsConstructor
class DomainModelMapper {

    private final PatronBooksFactory patronBooksFactory;

    PatronBooks map(PatronBooksDatabaseEntity entity) {
        return patronBooksFactory.recreateFrom(
                mapPatronInformation(entity),
                mapPatronHolds(entity),
                mapPatronOverdueCheckouts(entity)
        );
    }

    OverdueCheckouts mapPatronOverdueCheckouts(PatronBooksDatabaseEntity patronBooksDatabaseEntity) {
        return new OverdueCheckouts(
                patronBooksDatabaseEntity
                .overdueCheckouts
                .stream()
                .collect(groupingBy(CheckoutDatabaseEntity::getLibraryBranchId, toSet()))
                .entrySet()
                .stream()
                .collect(toMap(
                        (Entry<UUID,Set<CheckoutDatabaseEntity>> entry) -> new LibraryBranchId(entry.getKey()),
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
