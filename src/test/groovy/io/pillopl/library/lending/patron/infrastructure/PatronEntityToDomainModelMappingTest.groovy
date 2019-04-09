package io.pillopl.library.lending.patron.infrastructure

import io.pillopl.library.catalogue.BookId
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId
import io.pillopl.library.lending.patron.model.PatronFactory
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.PatronType
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronType.Regular
import static java.util.Collections.emptyList

class PatronEntityToDomainModelMappingTest extends Specification {

    DomainModelMapper domainModelMapper = new DomainModelMapper(new PatronFactory())

    LibraryBranchId libraryBranchId = anyBranch()
    LibraryBranchId anotherBranchId = anyBranch()
    PatronId patronId = anyPatronId()
    BookId bookId = anyBookId()
    BookId anotherBookId = anyBookId()
    Instant anyDate = Instant.now()

    def 'should map patron holds'() {
        given:
            PatronDatabaseEntity entity = patronEntity(patronId, Regular, [
                    new HoldDatabaseEntity(bookId.bookId, patronId.patronId, libraryBranchId.libraryBranchId, anyDate),
                    new HoldDatabaseEntity(anotherBookId.bookId, patronId.patronId, anotherBranchId.libraryBranchId, anyDate)])
        when:
            Set<Tuple2<BookId, LibraryBranchId>> patronHolds = domainModelMapper.mapPatronHolds(entity)
        then:
            patronHolds.size() == 2


    }

    def 'should map patron overdue checkouts'() {
        given:
            PatronDatabaseEntity entity = patronEntity(patronId, Regular, [], [
                    new OverdueCheckoutDatabaseEntity(bookId.bookId, patronId.patronId, libraryBranchId.libraryBranchId),
                    new OverdueCheckoutDatabaseEntity(anotherBookId.bookId, patronId.patronId, anotherBranchId.libraryBranchId)])
        when:
            Map<LibraryBranchId, Set<BookId>> overdueCheckouts = domainModelMapper.mapPatronOverdueCheckouts(entity)
        then:
            overdueCheckouts.get(libraryBranchId).size() == 1
            overdueCheckouts.get(anotherBranchId).size() == 1
    }


    PatronDatabaseEntity patronEntity(PatronId patronId,
                                      PatronType type,
                                      List<HoldDatabaseEntity> holds = emptyList(),
                                      List<OverdueCheckoutDatabaseEntity> overdueCheckouts = emptyList()) {
        PatronDatabaseEntity entity = new PatronDatabaseEntity(patronId, type)
        entity.booksOnHold = holds as Set
        entity.checkouts = overdueCheckouts as Set
        return entity
    }

}
