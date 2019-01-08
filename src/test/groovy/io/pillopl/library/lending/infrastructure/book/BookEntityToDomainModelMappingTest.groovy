package io.pillopl.library.lending.infrastructure.book

import io.pillopl.library.lending.domain.book.*
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.infrastructure.book.BookDatabaseEntity.BookState
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.book.BookFixture.circulatingAvailableBookAt
import static io.pillopl.library.lending.domain.book.BookType.Circulating
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.anyPatronId
import static io.pillopl.library.lending.infrastructure.book.BookDatabaseEntity.BookState.*

class BookEntityToDomainModelMappingTest extends Specification {

    LibraryBranchId libraryBranchId = anyBranch()
    LibraryBranchId anotherBranchId = anyBranch()
    LibraryBranchId yetAnotherBranchId = anyBranch()
    PatronId patronId = anyPatronId()
    PatronId anotherPatronId = anyPatronId()
    BookId bookId = anyBookId()
    Instant holdTill = Instant.now()

    def 'should create a new instance with id, isbn and book type from domain model'() {
        given:
            AvailableBook availableBook = circulatingAvailableBookAt(bookId, libraryBranchId)
        when:
            BookDatabaseEntity entity = BookDatabaseEntity.from(availableBook)
        then:
            entity.bookId == bookId.bookId
            entity.bookType == Circulating
            entity.bookState == Available


    }

    def 'should update data model to on hold book'() {
        given:
            AvailableBook availableBook = circulatingAvailableBookAt(bookId, libraryBranchId)
        and:
            BookDatabaseEntity entity = BookDatabaseEntity.from(availableBook)
        and:
            BookOnHold bookOnHold =  new BookOnHold(new BookInformation(bookId, Circulating), libraryBranchId, patronId, holdTill)
        when:
            entity = entity.updateFromDomainModel(bookOnHold)
        then:
            entity.bookId == bookId.bookId
            entity.bookType == Circulating
            entity.bookState == OnHold
            entity.onHoldAtBranch == libraryBranchId.libraryBranchId
            entity.onHoldByPatron == patronId.patronId
            entity.onHoldTill == holdTill
    }

    def 'should update data model to collected book'() {
        given:
            AvailableBook availableBook = circulatingAvailableBookAt(bookId, libraryBranchId)
        and:
            BookDatabaseEntity entity = BookDatabaseEntity.from(availableBook)
        and:
            BookOnHold bookOnHold =  new BookOnHold(new BookInformation(bookId, Circulating), libraryBranchId, patronId, holdTill)
        and:
            entity = entity.updateFromDomainModel(bookOnHold)
        and:
            CollectedBook collectedBook = new CollectedBook(new BookInformation(bookId, Circulating), anotherBranchId, anotherPatronId)
        when:
            entity = entity.updateFromDomainModel(collectedBook)
        then:
            entity.bookId == bookId.bookId
            entity.bookType == Circulating
            entity.bookState == Collected
            entity.onHoldAtBranch == libraryBranchId.libraryBranchId
            entity.onHoldByPatron == patronId.patronId
            entity.onHoldTill == holdTill
            entity.collectedByPatron == anotherPatronId.patronId
            entity.collectedAtBranch == anotherBranchId.libraryBranchId
    }


    def 'should map to available book'() {
        given:
            BookDatabaseEntity entity = bookEntity(Available)
        when:
            Book book = entity.toDomainModel()
        and:
            AvailableBook availableBook = book as AvailableBook
        then:
            availableBook.bookId == bookId
            availableBook.bookInformation.bookType == Circulating
            availableBook.libraryBranch == libraryBranchId

    }

    def 'should map to on hold book'() {
        given:
            BookDatabaseEntity entity = bookEntity(OnHold)
        when:
            Book book = entity.toDomainModel()
        and:
            BookOnHold bookOnHold = book as BookOnHold
        then:
            bookOnHold.bookId == bookId
            bookOnHold.bookInformation.bookType == Circulating
            bookOnHold.holdPlacedAt == anotherBranchId
            bookOnHold.byPatron == patronId
            bookOnHold.holdTill == holdTill
    }

    def 'should map to collected book'() {
        given:
            BookDatabaseEntity entity = bookEntity(Collected)
        when:
            Book book = entity.toDomainModel()
        and:
            CollectedBook collectedBook = book as CollectedBook
        then:
            collectedBook.bookId == bookId
            collectedBook.bookInformation.bookType == Circulating
            collectedBook.collectedAt == yetAnotherBranchId
            collectedBook.byPatron == anotherPatronId
    }

    BookDatabaseEntity bookEntity(BookState state) {
        new BookDatabaseEntity(
                id: 1L,
                bookId: bookId.bookId,
                bookType: Circulating,
                bookState: state,
                availableAtBranch: libraryBranchId.libraryBranchId,
                onHoldAtBranch: anotherBranchId.libraryBranchId,
                onHoldByPatron: patronId.patronId,
                onHoldTill: holdTill,
                collectedAtBranch: yetAnotherBranchId.libraryBranchId,
                collectedByPatron: anotherPatronId.patronId)
    }
}
