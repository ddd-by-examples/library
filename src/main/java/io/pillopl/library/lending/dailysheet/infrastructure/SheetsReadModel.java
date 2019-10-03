package io.pillopl.library.lending.dailysheet.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.dailysheet.model.CheckoutsToOverdueSheet;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.dailysheet.model.ExpiredHold;
import io.pillopl.library.lending.dailysheet.model.HoldsToExpireSheet;
import io.pillopl.library.lending.dailysheet.model.OverdueCheckout;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.BookCheckedOut;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldCanceled;
import io.pillopl.library.lending.patron.model.PatronEvent.BookHoldExpired;
import io.pillopl.library.lending.patron.model.PatronEvent.BookPlacedOnHold;
import io.pillopl.library.lending.patron.model.PatronEvent.BookReturned;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Option;
import lombok.AccessLevel;
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

import static io.vavr.collection.List.ofAll;
import static java.sql.Timestamp.from;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class SheetsReadModel implements DailySheet {

    private final JdbcTemplate sheets;
    private final Clock clock;

    @Override
    public HoldsToExpireSheet queryForHoldsToExpireSheet() {
        return new HoldsToExpireSheet(ofAll(
                findHoldsToExpire()
                        .stream()
                        .map(this::toExpiredHold)
                        .collect(toList())));
    }

    private List<Map<String, Object>> findHoldsToExpire() {
        return sheets.query(
                "SELECT h.book_id, h.hold_by_patron_id, h.hold_at_branch FROM holds_sheet h WHERE h.status = 'ACTIVE' and h.hold_till <= ?",
                new Object[]{from(Instant.now(clock))},
                new ColumnMapRowMapper());
    }

    private ExpiredHold toExpiredHold(Map<String, Object> map) {
        return new ExpiredHold(
                new BookId((UUID) map.get("BOOK_ID")),
                new PatronId((UUID) map.get("HOLD_BY_PATRON_ID")),
                new LibraryBranchId((UUID) map.get("HOLD_AT_BRANCH")));
    }

    @Override
    public CheckoutsToOverdueSheet queryForCheckoutsToOverdue() {
        return new CheckoutsToOverdueSheet(ofAll(
                findCheckoutsToOverdue()
                        .stream()
                        .map(this::toOverdueCheckout)
                        .collect(toList())));
    }

    private List<Map<String, Object>> findCheckoutsToOverdue() {
        return sheets.query(
                "SELECT c.book_id, c.checked_out_by_patron_id, c.checked_out_at_branch FROM checkouts_sheet c WHERE c.status = 'CHECKEDOUT' and c.checkout_till <= ?",
                new Object[]{from(Instant.now(clock))},
                new ColumnMapRowMapper());
    }

    private OverdueCheckout toOverdueCheckout(Map<String, Object> map) {
        return new OverdueCheckout(
                new BookId((UUID) map.get("BOOK_ID")),
                new PatronId((UUID) map.get("CHECKED_OUT_BY_PATRON_ID")),
                new LibraryBranchId((UUID) map.get("CHECKED_OUT_AT_BRANCH")));
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
                        "(id, book_id, status, hold_event_id, hold_by_patron_id, hold_at, hold_till, expired_at, canceled_at, hold_at_branch, checked_out_at) VALUES " +
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
    public void handle(BookCheckedOut event) {
        try {
            createNewCheckout(event);
        } catch (DuplicateKeyException ex) {
            //idempotent operation
        }
    }

    private void createNewCheckout(BookCheckedOut event) {
        sheets.update("INSERT INTO checkouts_sheet " +
                        "(id, book_id, status, checkout_event_id, checked_out_by_patron_id, checked_out_at, checkout_till, checked_out_at_branch, returned_at) VALUES " +
                        "(checkouts_sheet_seq.nextval, ?, ?, ?, ?, ?, ?, ?, null)",
                event.getBookId(),
                "CHECKEDOUT",
                event.getEventId(),
                event.getPatronId(),
                from(event.getWhen()),
                from(event.getTill()),
                event.getLibraryBranchId());
        sheets.update("UPDATE holds_sheet SET checked_out_at = ?, status = 'CHECKEDOUT' WHERE checked_out_at IS NULL AND book_id = ? AND hold_by_patron_id = ?",
                from(event.getWhen()),
                event.getBookId(),
                event.getPatronId());
    }

    @Override
    @EventListener
    public void handle(BookReturned event) {
        int results = markAsReturned(event);
        if (results == 0) {
            insertAsReturnedWithCheckedOutEventMissing(event);
        }

    }

    private int markAsReturned(BookReturned event) {
        return sheets.update("UPDATE checkouts_sheet SET returned_at = ?, status = 'RETURNED' WHERE returned_at IS NULL AND book_id = ? AND checked_out_by_patron_id = ?",
                from(event.getWhen()),
                event.getBookId(),
                event.getPatronId());
    }

    private void insertAsReturnedWithCheckedOutEventMissing(BookReturned event) {
        sheets.update("INSERT INTO checkouts_sheet " +
                        "(id, book_id, status, checkout_event_id, checked_out_by_patron_id, checked_out_at, checked_out_till, returned_at) VALUES " +
                        "(checkouts_sheet_seq.nextval, ?, ?, ?, ?, null, null, ?)",
                event.getBookId(),
                "CHECKEDOUT",
                event.getEventId(),
                event.getPatronId(),
                from(event.getWhen()));
    }

}

