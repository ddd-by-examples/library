CREATE TABLE IF NOT EXISTS book_database_entity (
  id INTEGER IDENTITY PRIMARY KEY,
  book_id UUID UNIQUE,
  book_type VARCHAR(100) NOT NULL,
  book_state VARCHAR(100) NOT NULL,
  available_at_branch UUID,
  on_hold_at_branch UUID,
  on_hold_by_patron UUID,
  checked_out_at_branch UUID,
  checked_out_by_patron UUID,
  on_hold_till TIMESTAMP,
  version INTEGER);

CREATE SEQUENCE book_database_entity_seq;
