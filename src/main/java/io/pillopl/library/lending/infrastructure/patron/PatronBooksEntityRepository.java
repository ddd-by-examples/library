package io.pillopl.library.lending.infrastructure.patron;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PatronBooksEntityRepository extends CrudRepository<PatronBooksDatabaseEntity, Long> {

    @Query("SELECT p.* FROM patron_books_database_entity p where p.patron_id = :patronId")
    PatronBooksDatabaseEntity findByPatronId(@Param("patronId") UUID patronId);

    @Query("SELECT b.* FROM book_on_hold_database_entity b where b.till > :at")
    List<BookOnHoldDatabaseEntity> findHoldsExpiredAt(@Param("at") Instant at);

}
