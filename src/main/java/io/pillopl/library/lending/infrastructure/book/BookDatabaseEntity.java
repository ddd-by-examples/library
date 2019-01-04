package io.pillopl.library.lending.infrastructure.book;

import io.pillopl.library.lending.domain.book.*;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronId;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.lending.infrastructure.book.BookDatabaseEntity.BookState.*;
import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

@NoArgsConstructor
class BookDatabaseEntity {

    enum BookState {
        Available, OnHold, Collected
    }

    @Id
    Long id;
    UUID bookId;
    BookType bookType;
    BookState bookState;
    UUID availableAtBranch;
    UUID onHoldAtBranch;
    UUID onHoldByPatron;
    Instant onHoldTill;
    UUID collectedAtBranch;
    UUID collectedByPatron;

    static BookDatabaseEntity from(Book book) {
        BookDatabaseEntity entity = new BookDatabaseEntity();
        entity.bookId = book.bookId().getBookId();
        entity.bookType = book.getBookInformation().getBookType();
        entity = entity.updateFromDomainModel(book);
        return entity;
    }

    BookDatabaseEntity updateFromDomainModel(Book book) {
        return Match(book).of(
                Case($(instanceOf(AvailableBook.class)), this::availableBookUpdate),
                Case($(instanceOf(BookOnHold.class)), this::onHoldBookUpdate),
                Case($(instanceOf(CollectedBook.class)), this::collectedBookUpdate)

        );
    }

    Book toDomainModel() {
        return Match(bookState).of(
                Case($(Available), this::toAvailableBook),
                Case($(OnHold), this::toBookOnHold),
                Case($(Collected), this::toCollectedBook)
        );
    }

    BookDatabaseEntity availableBookUpdate(AvailableBook availableBook) {
        bookState = Available;
        availableAtBranch = availableBook.getLibraryBranch().getLibraryBranchId();
        return this;
    }

    AvailableBook toAvailableBook() {
        return new AvailableBook(bookInformation(), new LibraryBranchId(availableAtBranch));
    }

    BookDatabaseEntity onHoldBookUpdate(BookOnHold bookOnHold) {
        bookState = OnHold;
        onHoldAtBranch = bookOnHold.getHoldPlacedAt().getLibraryBranchId();
        onHoldByPatron = bookOnHold.getByPatron().getPatronId();
        onHoldTill = bookOnHold.getHoldTill();
        return this;
    }

    BookOnHold toBookOnHold() {
        return new BookOnHold(bookInformation(), new LibraryBranchId(onHoldAtBranch), new PatronId(onHoldByPatron), onHoldTill);
    }

    BookInformation bookInformation() {
        return new BookInformation(new BookId(bookId), bookType);
    }

    BookDatabaseEntity collectedBookUpdate(CollectedBook collectedBook) {
        bookState = Collected;
        collectedAtBranch = collectedBook.getCollectedAt().getLibraryBranchId();
        collectedByPatron = collectedBook.getByPatron().getPatronId();
        return this;
    }

    CollectedBook toCollectedBook() {
        return new CollectedBook(bookInformation(), new LibraryBranchId(collectedAtBranch), new PatronId(collectedByPatron));
    }
}

