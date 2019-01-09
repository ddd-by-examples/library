package io.pillopl.library.lending.infrastructure.checkout

import io.pillopl.library.lending.application.hold.FindHoldsToExpirePolicy
import io.pillopl.library.lending.domain.book.BookInformation
import io.pillopl.library.lending.domain.book.BookType
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronBooksRepository
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronInformation
import io.pillopl.library.lending.infrastructure.patron.PatronBooksEntityRepository
import io.pillopl.library.lending.infrastructure.patron.PatronDatabaseConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold
import static io.pillopl.library.lending.domain.patron.PatronBooksEvent.PatronCreated
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.domain.patron.PatronInformation.PatronType.Regular

@ContextConfiguration(classes = PatronDatabaseConfiguration.class)
@SpringBootTest
class FindHoldsToExpirePolicyDatabaseIT extends Specification {

    static final Instant TIME_OF_EXPIRE_CHECK = Instant.now()

    PatronId patronId = anyPatronId()
    LibraryBranchId libraryBranchId = anyBranch()
    BookInformation bookInformation = new BookInformation(anyBookId(), BookType.Restricted);

    @Autowired
    PatronBooksEntityRepository patronEntityRepository

    @Autowired
    PatronBooksRepository patronBooksRepository

    FindHoldsToExpirePolicy findHoldsToExpirePolicy

    def setup() {
        findHoldsToExpirePolicy = new FindExpiredHoldsInDatabaseByHoldDuration(
                patronEntityRepository,
                Clock.fixed(TIME_OF_EXPIRE_CHECK, ZoneId.systemDefault()))
    }

    def 'should find expired holds'() {
        given:
            int currentNoOfExpiredHolds = findHoldsToExpirePolicy.allHoldsToExpire().count()
        and:
            patronBooksRepository.handle(patronCreated())
        when:
            patronBooksRepository.handle(placedOnHold(aCloseEndedHoldTillYesterday()))
        and:
            patronBooksRepository.handle(placedOnHold(aCloseEndedHoldTillTomorrow()))
        then:
            findHoldsToExpirePolicy.allHoldsToExpire().count() == currentNoOfExpiredHolds + 1
    }

    def 'should never find open-ended holds'() {
        given:
            int currentNoOfExpiredHolds = findHoldsToExpirePolicy.allHoldsToExpire().count()
        and:
            patronBooksRepository.handle(patronCreated())
        when:
            patronBooksRepository.handle(placedOnHold(anOpenEndedHold()))
        then:
            findHoldsToExpirePolicy.allHoldsToExpire().count() == currentNoOfExpiredHolds
    }

    Instant aCloseEndedHoldTillTomorrow() {
        return TIME_OF_EXPIRE_CHECK.plus(Duration.ofDays(1))
    }

    Instant aCloseEndedHoldTillYesterday() {
        return TIME_OF_EXPIRE_CHECK.minus(Duration.ofDays(1))
    }

    Instant anOpenEndedHold() {
        return null
    }

    BookPlacedOnHold placedOnHold(Instant till) {
        BookPlacedOnHold placedOnHold =
                new BookPlacedOnHold(
                        Instant.now(),
                        patronId.getPatronId(),
                        bookInformation.getBookId().getBookId(),
                        bookInformation.bookType,
                        libraryBranchId.getLibraryBranchId(),
                        TIME_OF_EXPIRE_CHECK.minusSeconds(60000),
                        till)
        return placedOnHold

    }

    PatronCreated patronCreated() {
        return PatronCreated.now(new PatronInformation(patronId, Regular))
    }

}
