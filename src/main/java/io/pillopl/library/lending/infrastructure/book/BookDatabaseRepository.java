package io.pillopl.library.lending.infrastructure.book;

import io.pillopl.library.commons.aggregates.AggregateRootIsStale;
import io.pillopl.library.lending.application.hold.FindAvailableBook;
import io.pillopl.library.lending.application.hold.FindBookOnHold;
import io.pillopl.library.lending.domain.book.*;
import io.pillopl.library.lending.domain.patron.PatronId;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.UUID;

import static io.pillopl.library.lending.infrastructure.book.BookDatabaseEntity.BookState.*;
import static io.vavr.API.*;
import static io.vavr.Patterns.$Some;
import static io.vavr.Predicates.instanceOf;
import static io.vavr.control.Option.*;

@AllArgsConstructor
class BookDatabaseRepository implements BookRepository, FindAvailableBook, FindBookOnHold {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Option<Book> findBy(BookId bookId) {
        return findBookById(bookId)
                .map(BookDatabaseEntity::toDomainModel);
    }

    private Option<BookDatabaseEntity> findBookById(BookId bookId) {
        return Try
                .ofSupplier(() -> of(jdbcTemplate.queryForObject("SELECT b.* FROM book_database_entity b WHERE b.book_id = ?", new BeanPropertyRowMapper<>(BookDatabaseEntity.class), bookId.getBookId())))
                .getOrElse(none());
    }

    @Override
    public Try<Void> save(Book book) {
        return Try.run(() -> {
            findBy(book.bookId())
                    .map(entity -> updateOptimistically(book))
                    .onEmpty(() -> insertNew(book));
        });
    }

    private int updateOptimistically(Book book) {
        int result = Match(book).of(
                Case($(instanceOf(AvailableBook.class)), this::update),
                Case($(instanceOf(BookOnHold.class)), this::update),
                Case($(instanceOf(CollectedBook.class)), this::update)
        );
        if (result == 0) {
            throw new AggregateRootIsStale("Someone has updated book in the meantime, book: " + book);
        }
        return result;
    }

    private int update(AvailableBook availableBook) {
        return jdbcTemplate.update("UPDATE book_database_entity b SET b.book_state = ?, b.available_at_branch = ?, b.version = ? WHERE book_id = ? AND version = ?",
                Available.toString(),
                availableBook.getLibraryBranch().getLibraryBranchId(),
                availableBook.getVersion().getVersion() + 1,
                availableBook.getBookId().getBookId(),
                availableBook.getVersion().getVersion());
    }

    private int update(BookOnHold bookOnHold) {
        return jdbcTemplate.update("UPDATE book_database_entity b SET b.book_state = ?, b.on_hold_at_branch = ?, b.on_hold_by_patron = ?, b.on_hold_till = ?, b.version = ? WHERE book_id = ? AND version = ?",
                OnHold.toString(),
                bookOnHold.getHoldPlacedAt().getLibraryBranchId(),
                bookOnHold.getByPatron().getPatronId(),
                bookOnHold.getHoldTill(),
                bookOnHold.getVersion().getVersion() + 1,
                bookOnHold.getBookId().getBookId(),
                bookOnHold.getVersion().getVersion());
    }

    private int update(CollectedBook collectedBook) {
        return jdbcTemplate.update("UPDATE book_database_entity b SET b.book_state = ?, b.collected_at_branch = ?, b.collected_by_patron = ?, b.version = ? WHERE book_id = ? AND version = ?",
                Collected.toString(),
                collectedBook.getCollectedAt().getLibraryBranchId(),
                collectedBook.getByPatron().getPatronId(),
                collectedBook.getVersion().getVersion() + 1,
                collectedBook.getBookId().getBookId(),
                collectedBook.getVersion().getVersion());
    }

    private void insertNew(Book book) {
        Match(book).of(
                Case($(instanceOf(AvailableBook.class)), this::insert),
                Case($(instanceOf(BookOnHold.class)), this::insert),
                Case($(instanceOf(CollectedBook.class)), this::insert)
        );
    }

    private int insert(AvailableBook availableBook) {
        return insert(availableBook.getBookInformation(), Available, availableBook.getLibraryBranch().getLibraryBranchId(), null, null, null, null, null);
    }

    private int insert(BookOnHold bookOnHold) {
        return insert(bookOnHold.getBookInformation(), OnHold, null, bookOnHold.getHoldPlacedAt().getLibraryBranchId(), bookOnHold.getByPatron().getPatronId(), bookOnHold.getHoldTill(), null, null);

    }

    private int insert(CollectedBook collectedBook) {
        return insert(collectedBook.getBookInformation(), Collected, null, null, null, null, collectedBook.getCollectedAt().getLibraryBranchId(), collectedBook.getByPatron().getPatronId());

    }

    private int insert(BookInformation bookInformation, BookDatabaseEntity.BookState state, UUID availableAt, UUID onHoldAt, UUID onHoldBy, Instant onHoldTill, UUID collectedAt, UUID collectedBy) {
        return jdbcTemplate.update("INSERT INTO book_database_entity " +
                        "(id, " +
                        "book_id, " +
                        "book_type, " +
                        "book_state, " +
                        "available_at_branch," +
                        "on_hold_at_branch, " +
                        "on_hold_by_patron, " +
                        "on_hold_till, " +
                        "collected_at_branch, " +
                        "collected_by_patron, " +
                        "version) VALUES " +
                        "(book_database_entity_seq.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0)",
                bookInformation.getBookId().getBookId(), bookInformation.getBookType().toString(), state.toString(), availableAt, onHoldAt, onHoldBy, onHoldTill, collectedAt, collectedBy);
    }

    @Override
    public Option<AvailableBook> findAvailableBookBy(BookId bookId) {
        return Match(findBy(bookId)).of(
                Case($Some($(instanceOf(AvailableBook.class))), Option::of),
                Case($(), Option::none)
        );
    }

    @Override
    public Option<BookOnHold> findBookOnHold(BookId bookId, PatronId patronId) {
        Option<BookOnHold> bookOnHold = Match(findBy(bookId)).of(
                Case($Some($(instanceOf(BookOnHold.class))), Option::of),
                Case($(), Option::none)
        );
        return bookOnHold.filter(onHold -> onHold.by(patronId));
    }

}

