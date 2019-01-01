package io.pillopl.library.lending.infrastructure.patron;


import io.pillopl.library.lending.domain.patron.PatronInformation;
import io.pillopl.library.lending.domain.patron.PatronInformation.PatronType;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookCollected;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent.BookPlacedOnHold;
import io.vavr.API;
import io.vavr.Predicates;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static io.vavr.API.$;
import static io.vavr.API.Case;

@NoArgsConstructor
class PatronBooksDatabaseEntity {

    @Id
    Long id;
    UUID patronId;
    PatronType patronType;
    Set<BookOnHoldDatabaseEntity> booksOnHold;

    PatronBooksDatabaseEntity(PatronInformation patronInformation) {
        this.patronId = patronInformation.getPatronId().getPatronId();
        this.patronType = patronInformation.getType();
        this.booksOnHold = new HashSet<>();
    }

    PatronBooksDatabaseEntity reactTo(PatronBooksEvent event) {
        return API.Match(event).of(
                Case($(Predicates.instanceOf(BookPlacedOnHold.class)), this::handle),
                Case($(Predicates.instanceOf(BookCollected.class)), this::handle)

        );
    }

    private PatronBooksDatabaseEntity handle(BookPlacedOnHold event) {
        booksOnHold.add(new BookOnHoldDatabaseEntity(event.getBookId(), event.getPatronId(), event.getLibraryBranchId()));
        return this;
    }

    private PatronBooksDatabaseEntity handle(BookCollected event) {
        booksOnHold
                .stream()
                .filter(entity -> entity.hasSamePropertiesAs(event))
                .findAny()
                .ifPresent(entity -> booksOnHold.remove(entity));
        return this;
    }

}


@NoArgsConstructor
@EqualsAndHashCode
class BookOnHoldDatabaseEntity {
    @Id
    Long id;
    UUID patronId;
    UUID bookId;
    UUID libraryBranchId;

    BookOnHoldDatabaseEntity(UUID bookId, UUID patronId, UUID libraryBranchId) {
        this.bookId = bookId;
        this.patronId = patronId;
        this.libraryBranchId = libraryBranchId;
    }

    boolean hasSamePropertiesAs(BookCollected event) {
        return  this.patronId.equals(event.getPatronId()) &&
                this.bookId.equals(event.getBookId()) &&
                this.libraryBranchId.equals(event.getLibraryBranchId());
    }

}

class OverdueCheckoutDatabaseEntity {
    @Id
    Long id;
    UUID resourceId;
    UUID libraryBranchId;

}