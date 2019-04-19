CREATE TABLE IF NOT EXISTS checkouts_sheet (
id INTEGER IDENTITY PRIMARY KEY,
  book_id UUID NOT NULL,
  status VARCHAR(20) NOT NULL,
  checkout_event_id UUID UNIQUE,
  checked_out_by_patron_id UUID,
  checked_out_at TIMESTAMP,
  returned_at TIMESTAMP,
  checked_out_at_branch UUID,
  checkout_till TIMESTAMP);


CREATE TABLE IF NOT EXISTS holds_sheet (
id INTEGER IDENTITY PRIMARY KEY,
  book_id UUID NOT NULL,
  status VARCHAR(20) NOT NULL,
  hold_event_id UUID UNIQUE,
  hold_at_branch UUID,
  hold_by_patron_id UUID,
  hold_at TIMESTAMP,
  hold_till TIMESTAMP,
  expired_at TIMESTAMP,
  canceled_at TIMESTAMP,
  checked_out_at TIMESTAMP);

CREATE SEQUENCE holds_sheet_seq;
CREATE SEQUENCE checkouts_sheet_seq;

