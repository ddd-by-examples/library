package io.pillopl.library.lending.dailysheet.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.dailysheet.model.CheckoutsToOverdueSheet;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.dailysheet.model.HoldsToExpireSheet;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.*;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.Tuple3;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.vavr.Tuple.of;
import static io.vavr.collection.List.ofAll;
import static java.sql.Timestamp.from;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
class SheetsReadModel implements DailySheet {

    private final JdbcTemplate sheets;
    private final Clock clock;

    @Override
    public HoldsToExpireSheet queryForHoldsToExpireSheet() {
        return new HoldsToExpireSheet(ofAll(
                findHoldsToExpire()
                        .stream()
                        .map(this::toExpiredHoldsTuple)
                        .collect(toList())));
    }

    private List<Map<String, Object>> findHoldsToExpire() {
        return sheets.query(
                "SELECT h.book_id, h.hold_by_patron_id, h.hold_at_branch FROM holds_sheet h WHERE h.status = 'ACTIVE' and h.hold_till <= ?",
                new Object[]{from(Instant.now(clock))},
                new ColumnMapRowMapper());
    }

    private Tuple3<BookId, PatronId, LibraryBranchId> toExpiredHoldsTuple(Map<String, Object> map) {
        return of(
                new BookId((UUID) map.get("BOOK_ID")),
                new PatronId((UUID) map.get("HOLD_BY_PATRON_ID")),
                new LibraryBranchId((UUID) map.get("HOLD_AT_BRANCH")));
    }

    @Override
    public CheckoutsToOverdueSheet queryForCheckoutsToOverdue() {
        return new CheckoutsToOverdueSheet(ofAll(
                findCheckoutsToOverdue()
                        .stream()
                        .map(this::toOverdueCheckoutsTuple)
                        .collect(toList())));
    }

    private List<Map<String, Object>> findCheckoutsToOverdue() {
        return sheets.query(
                "SELECT c.book_id, c.collected_by_patron_id, c.collected_at_branch FROM checkouts_sheet c WHERE c.status = 'COLLECTED' and c.checkout_till <= ?",
                new Object[]{from(Instant.now(clock))},
                new ColumnMapRowMapper());
    }

    private Tuple3<BookId, PatronId, LibraryBranchId> toOverdueCheckoutsTuple(Map<String, Object> map) {
        return of(
                new BookId((UUID) map.get("BOOK_ID")),
                new PatronId((UUID) map.get("COLLECTED_BY_PATRON_ID")),
                new LibraryBranchId((UUID) map.get("COLLECTED_AT_BRANCH")));
    }

    @Override
    @Transactional
    @EventListener
    public void handle(BookPlacedOnHold event) {
        try {
            createNewHold(event);
        } catch (DuplicateKeyException ex) {
            //idempotent operation
        }
    }

    private void createNewHold(BookPlacedOnHold event) {
        sheets.update("INSERT INTO holds_sheet " +
                        "(id, book_id, status, hold_event_id, hold_by_patron_id, hold_at, hold_till, expired_at, canceled_at, hold_at_branch, collected_at) VALUES " +
                        "(holds_sheet_seq.nextval, ?, ?, ?, ?, ?, ?, null, null, ?, null)",
                event.getBookId(),
                "ACTIVE",
                event.getEventId(),
                event.getPatronId(),
                from(event.getWhen()),
                Option.of(event.getHoldTill()).map(Timestamp::from).getOrNull(),
                event.getLibraryBranchId());
    }


    @Override
    public void handle(BookHoldCanceled event) {
        sheets.update("UPDATE holds_sheet SET canceled_at = ?, status = 'CANCELED' WHERE canceled_at IS NULL AND book_id = ? AND hold_by_patron_id = ?",
                from(event.getWhen()),
                event.getBookId(),
                event.getPatronId());
    }

    @Override
    @EventListener
    public void handle(BookHoldExpired event) {
        sheets.update("UPDATE holds_sheet SET expired_at = ?, status = 'EXPIRED' WHERE expired_at IS NULL AND book_id = ? AND hold_by_patron_id = ?",
                from(event.getWhen()),
                event.getBookId(),
                event.getPatronId());
    }

    @Override
    @EventListener
    public void handle(BookCollected event) {
        try {
            createNewCheckout(event);
        } catch (DuplicateKeyException ex) {
            //idempotent operation
        }
    }

    private void createNewCheckout(BookCollected event) {
        sheets.update("INSERT INTO checkouts_sheet " +
                        "(id, book_id, status, checkout_event_id, collected_by_patron_id, collected_at, checkout_till, collected_at_branch, returned_at) VALUES " +
                        "(checkouts_sheet_seq.nextval, ?, ?, ?, ?, ?, ?, ?, null)",
                event.getBookId(),
                "COLLECTED",
                event.getEventId(),
                event.getPatronId(),
                from(event.getWhen()),
                from(event.getTill()),
                event.getLibraryBranchId());
        sheets.update("UPDATE holds_sheet SET collected_at = ?, status = 'COLLECTED' WHERE collected_at IS NULL AND book_id = ? AND hold_by_patron_id = ?",
                from(event.getWhen()),
                event.getBookId(),
                event.getPatronId());
    }

    @Override
    @EventListener
    public void handle(BookReturned event) {
        int results = markAsReturned(event);
        if (results == 0) {
            insertAsReturnedWithCollectedEventMissing(event);
        }

    }

    private int markAsReturned(BookReturned event) {
        return sheets.update("UPDATE checkouts_sheet SET returned_at = ?, status = 'RETURNED' WHERE returned_at IS NULL AND book_id = ? AND collected_by_patron_id = ?",
                from(event.getWhen()),
                event.getBookId(),
                event.getPatronId());
    }

    private void insertAsReturnedWithCollectedEventMissing(BookReturned event) {
        sheets.update("INSERT INTO checkouts_sheet " +
                        "(id, book_id, status, checkout_event_id, collected_by_patron_id, collected_at, collected_till, returned_at) VALUES " +
                        "(checkouts_sheet_seq.nextval, ?, ?, ?, ?, null, null, ?)",
                event.getBookId(),
                "COLLECTED",
                event.getEventId(),
                event.getPatronId(),
                from(event.getWhen()));
    }

}

