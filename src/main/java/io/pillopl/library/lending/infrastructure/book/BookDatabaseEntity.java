package io.pillopl.library.lending.infrastructure.book;

import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.domain.book.*;
import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronId;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.lending.infrastructure.book.BookDatabaseEntity.BookState.*;
import static io.vavr.API.*;

@NoArgsConstructor
@Data
class BookDatabaseEntity {

    enum BookState {
        Available, OnHold, Collected
    }

    UUID book_id;
    BookType book_type;
    BookState book_state;
    UUID available_at_branch;
    UUID on_hold_at_branch;
    UUID on_hold_by_patron;
    Instant on_hold_till;
    UUID collected_at_branch;
    UUID collected_by_patron;
    int version;

    Book toDomainModel() {
        return Match(book_state).of(
                Case($(Available), this::toAvailableBook),
                Case($(OnHold), this::toBookOnHold),
                Case($(Collected), this::toCollectedBook)
        );
    }

    private AvailableBook toAvailableBook() {
        return new AvailableBook(bookInformation(), new LibraryBranchId(available_at_branch), new Version(version));
    }

    private BookOnHold toBookOnHold() {
        return new BookOnHold(bookInformation(), new LibraryBranchId(on_hold_at_branch), new PatronId(on_hold_by_patron), on_hold_till, new Version(version));
    }

    private BookInformation bookInformation() {
        return new BookInformation(new BookId(book_id), book_type);
    }


    private CollectedBook toCollectedBook() {
        return new CollectedBook(bookInformation(), new LibraryBranchId(collected_at_branch), new PatronId(collected_by_patron), new Version(version));
    }
}

