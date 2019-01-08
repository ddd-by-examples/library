package io.pillopl.library.lending.infrastructure.patron

import io.pillopl.library.lending.domain.book.BookInformation
import io.pillopl.library.lending.domain.book.BookType
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronBooksFactory
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronInformation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.*
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular

@ContextConfiguration(classes = PatronDatabaseConfiguration.class)
@SpringBootTest
class FindExpiredHoldsDatabaseIT extends Specification {

    static final Instant TIME_OF_EXPIRE_CHECK = Instant.now()

    PatronId patronId = anyPatronId()
    LibraryBranchId libraryBranchId = anyBranch()
    BookInformation bookInformation = new BookInformation(anyBookId(), BookType.Restricted);

    @Autowired
    PatronBooksEntityRepository patronEntityRepository

    PatronBooksDatabaseRepository patronBooksRepo

    def setup() {
         patronBooksRepo = new PatronBooksDatabaseRepository(
                patronEntityRepository,
                new PatronBooksFactory(),
                new DomainModelMapper(),
                Clock.fixed(TIME_OF_EXPIRE_CHECK, ZoneId.systemDefault()))
    }


    def 'should find expired holds'() {
        given:
            patronBooksRepo.handle(patronCreated())
        when:
            patronBooksRepo.handle(placedOnHold(anExpiredHold()))
        and:
            patronBooksRepo.handle(placedOnHold(nonExpiredHold()))
        then:
            patronBooksRepo.allExpiredHolds().expiredHolds.size() == 1
    }

    Instant nonExpiredHold() {
        TIME_OF_EXPIRE_CHECK.plusSeconds(60)
    }

    Instant anExpiredHold() {
        TIME_OF_EXPIRE_CHECK.minusSeconds(60)
    }

    BookPlacedOnHold placedOnHold(Instant till) {
        BookPlacedOnHold placedOnHold =
                new BookPlacedOnHold(
                        Instant.now(),
                        patronId.getPatronId(),
                        bookInformation.getBookId().getBookId(),
                        bookInformation.bookType,
                        libraryBranchId.getLibraryBranchId(),
                        till.minusSeconds(60),
                        till)
        return placedOnHold

    }

    PatronCreated patronCreated() {
        return PatronCreated.now(new PatronInformation(patronId, Regular))
    }

}
