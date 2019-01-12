package io.pillopl.library.lending.infrastructure.book

import io.pillopl.library.lending.domain.book.*
import io.pillopl.library.lending.domain.library.LibraryBranchId
import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.infrastructure.book.BookDatabaseEntity.BookState
import spock.lang.Specification

import java.time.Instant

import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
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
                book_id: bookId.bookId,
                book_type: Circulating,
                book_state: state,
                available_at_branch: libraryBranchId.libraryBranchId,
                on_hold_at_branch: anotherBranchId.libraryBranchId,
                on_hold_by_patron: patronId.patronId,
                on_hold_till: holdTill,
                collected_at_branch: yetAnotherBranchId.libraryBranchId,
                collected_by_patron: anotherPatronId.patronId)
    }
}
