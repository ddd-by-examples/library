package io.pillopl.library.lending.infrastructure.patron;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.*;
import io.pillopl.library.lending.domain.book.BookId;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular;
import static java.util.stream.Collectors.toSet;

class PatronBooksDatabaseRepository implements PatronBooksRepository {

    private final PatronBooksEntityRepository patronBooksEntityRepository;
    private final PatronBooksFactory patronBooksFactory;

    PatronBooksDatabaseRepository(PatronBooksEntityRepository patronBooksEntityRepository,
                                  PatronBooksFactory patronBooksFactory) {
        this.patronBooksEntityRepository = patronBooksEntityRepository;
        this.patronBooksFactory = patronBooksFactory;
    }

    @Override
    public Option<PatronBooks> findBy(PatronId patronId) {
        return patronBooksEntityRepository
                .findByPatronId(patronId.getPatronId())
                .map(this::recreateDomainModelFromDataModel);
    }

    private PatronBooks recreateDomainModelFromDataModel(PatronBooksDatabaseEntity patronBooksDatabaseEntity) {
        PatronInformation patronInformation =
                new PatronInformation(new PatronId(patronBooksDatabaseEntity.patronId), patronBooksDatabaseEntity.patronType);
        PatronHolds patronHolds =
                new PatronHolds(patronBooksDatabaseEntity
                        .booksOnHold
                        .stream()
                        .map(entity -> new PatronHold(new BookId(entity.bookId), new LibraryBranchId(entity.libraryBranchId)))
                        .collect(toSet()));
        return patronBooksFactory.recreateFrom(patronInformation, patronHolds);
    }

    @Override
    public Try<Void> reactTo(PatronBooksEvent domainEvent) {
        //TODO add optimistic locking
        return Try.run(() -> {
            PatronBooksDatabaseEntity dataModel = findOrCreateNewFor(domainEvent.patronId());
            dataModel.reactTo(domainEvent);
            patronBooksEntityRepository.save(dataModel);
        });
    }

    private PatronBooksDatabaseEntity findOrCreateNewFor(PatronId patronId) {
        //TODO change regular
        return patronBooksEntityRepository.findByPatronId(patronId.getPatronId())
                .getOrElse(() -> new PatronBooksDatabaseEntity(new PatronInformation(patronId, Regular)));
    }

}

interface PatronBooksEntityRepository extends CrudRepository<PatronBooksDatabaseEntity, Long> {

    @Query("SELECT p.* FROM patron_books_database_entity p where p.patron_id = :patronId")
    Option<PatronBooksDatabaseEntity> findByPatronId(@Param("patronId") UUID patronId);

}
