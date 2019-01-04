CREATE TABLE IF NOT EXISTS patron_books_database_entity (id INTEGER IDENTITY PRIMARY KEY, patron_type VARCHAR(100), patron_id UUID UNIQUE);

CREATE TABLE IF NOT EXISTS book_on_hold_database_entity (id INTEGER IDENTITY PRIMARY KEY, book_id UUID, patron_id UUID, library_branch_id UUID, patron_books_database_entity INTEGER);

CREATE TABLE IF NOT EXISTS overdue_checkout_database_entity (id INTEGER IDENTITY PRIMARY KEY, book_id UUID, patron_id UUID, library_branch_id UUID, patron_books_database_entity INTEGER);
