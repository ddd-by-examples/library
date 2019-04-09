package io.pillopl.library.lending.patronprofile.infrastructure;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patronprofile.model.CheckoutsView;
import io.pillopl.library.lending.patronprofile.model.HoldsView;
import io.pillopl.library.lending.patronprofile.model.PatronProfile;
import io.pillopl.library.lending.patronprofile.model.PatronProfiles;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.vavr.collection.List.ofAll;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor
class PatronProfileReadModel implements PatronProfiles {

    private final JdbcTemplate sheets;

    @Override
    public PatronProfile fetchFor(PatronId patronId) {
        HoldsView holdsView = new HoldsView(
                ofAll(findCurrentHoldsFor(patronId)
                        .stream()
                        .map(this::toHoldViewTuple)
                        .collect(toList())));
        CheckoutsView checkoutsView = new CheckoutsView(
                ofAll(findCurrentCheckoutsFor(patronId)
                        .stream()
                        .map(this::toCheckoutsViewTuple)
                        .collect(toList())));
        return new PatronProfile(holdsView, checkoutsView);
    }

    private List<Map<String, Object>> findCurrentHoldsFor(PatronId patronId) {
        return sheets.query(
                "SELECT h.book_id, h.hold_till FROM holds_sheet h WHERE h.hold_by_patron_id = ? AND h.collected_at IS NULL AND h.expired_at IS NULL AND h.canceled_at IS NULL",
                new Object[]{patronId.getPatronId()},
                new ColumnMapRowMapper());
    }

    private Tuple2<BookId, Instant> toHoldViewTuple(Map<String, Object> map) {
        return Tuple.of(new BookId((UUID) map.get("BOOK_ID")),
                ((Timestamp) map.get("HOLD_TILL")).toInstant());
    }

    private List<Map<String, Object>> findCurrentCheckoutsFor(PatronId patronId) {
        return sheets.query(
                "SELECT h.book_id, h.checkout_till FROM checkouts_sheet h WHERE h.collected_by_patron_id = ? AND h.returned_at IS NULL",
                new Object[]{patronId.getPatronId()},
                new ColumnMapRowMapper());
    }

    private Tuple2<BookId, Instant> toCheckoutsViewTuple(Map<String, Object> map) {
        return Tuple.of(new BookId((UUID) map.get("BOOK_ID")),
                ((Timestamp) map.get("CHECKOUT_TILL")).toInstant());
    }
}

