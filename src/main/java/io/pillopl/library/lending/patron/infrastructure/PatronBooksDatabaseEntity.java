package io.pillopl.library.lending.patron.infrastructure;


import io.pillopl.library.lending.patron.model.PatronBooksEvent;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.*;
import io.pillopl.library.lending.patron.model.PatronInformation;
import io.pillopl.library.lending.patron.model.PatronInformation.PatronType;
import io.vavr.API;
import io.vavr.Predicates;
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
    Set<OverdueCheckoutDatabaseEntity> checkouts;

    PatronBooksDatabaseEntity(PatronInformation patronInformation) {
        this.patronId = patronInformation.getPatronId().getPatronId();
        this.patronType = patronInformation.getType();
        this.booksOnHold = new HashSet<>();
        this.checkouts = new HashSet<>();
    }

    PatronBooksDatabaseEntity handle(PatronBooksEvent event) {
        return API.Match(event).of(
                Case($(Predicates.instanceOf(BookPlacedOnHoldEvents.class)), this::handle),
                Case($(Predicates.instanceOf(BookPlacedOnHold.class)), this::handle),
                Case($(Predicates.instanceOf(BookCollected.class)), this::handle),
                Case($(Predicates.instanceOf(BookHoldCanceled.class)), this::handle),
                Case($(Predicates.instanceOf(BookHoldExpired.class)), this::handle),
                Case($(Predicates.instanceOf(OverdueCheckoutRegistered.class)), this::handle),
                Case($(Predicates.instanceOf(BookReturned.class)), this::handle)

        );
    }

    private PatronBooksDatabaseEntity handle(BookPlacedOnHoldEvents placedOnHoldEvents) {
        BookPlacedOnHold event = placedOnHoldEvents.getBookPlacedOnHold();
        return handle(event);
    }

    private PatronBooksDatabaseEntity handle(BookPlacedOnHold event) {
        booksOnHold.add(new BookOnHoldDatabaseEntity(event.getBookId(), event.getPatronId(), event.getLibraryBranchId(), event.getHoldTill()));
        return this;
    }

    private PatronBooksDatabaseEntity handle(BookHoldCanceled event) {
        return removeHoldIfPresent(event.getPatronId(), event.getBookId(), event.getLibraryBranchId());
    }


    private PatronBooksDatabaseEntity handle(BookCollected event) {
        return removeHoldIfPresent(event.getPatronId(), event.getBookId(), event.getLibraryBranchId());
    }

    private PatronBooksDatabaseEntity handle(BookHoldExpired event) {
        return removeHoldIfPresent(event.getPatronId(), event.getBookId(), event.getLibraryBranchId());
    }

    private PatronBooksDatabaseEntity handle(OverdueCheckoutRegistered event) {
        checkouts.add(new OverdueCheckoutDatabaseEntity(event.getBookId(), event.getPatronId(), event.getLibraryBranchId()));
        return this;
    }

    private PatronBooksDatabaseEntity handle(BookReturned event) {
        return removeOverdueCheckoutIfPresent(event.getPatronId(), event.getBookId(), event.getLibraryBranchId());
    }

    private PatronBooksDatabaseEntity removeHoldIfPresent(UUID patronId, UUID bookId, UUID libraryBranchId) {
        booksOnHold
                .stream()
                .filter(entity -> entity.is(patronId, bookId, libraryBranchId))
                .findAny()
                .ifPresent(entity -> booksOnHold.remove(entity));
        return this;
    }

    private PatronBooksDatabaseEntity removeOverdueCheckoutIfPresent(UUID patronId, UUID bookId, UUID libraryBranchId) {
        checkouts
                .stream()
                .filter(entity -> entity.is(patronId, bookId, libraryBranchId))
                .findAny()
                .ifPresent(entity -> checkouts.remove(entity));
        return this;
    }

}


