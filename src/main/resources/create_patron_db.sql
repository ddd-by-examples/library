CREATE TABLE IF NOT EXISTS patron_books_database_entity (id INTEGER IDENTITY PRIMARY KEY, patron_type VARCHAR(100) NOT NULL, patron_id UUID UNIQUE);

CREATE TABLE IF NOT EXISTS book_on_hold_database_entity (id INTEGER IDENTITY PRIMARY KEY, book_id UUID NOT NULL, patron_id UUID NOT NULL, library_branch_id UUID NOT NULL, patron_books_database_entity INTEGER NOT NULL, till TIMESTAMP NOT NULL);

CREATE TABLE IF NOT EXISTS overdue_checkout_database_entity (id INTEGER IDENTITY PRIMARY KEY, book_id UUID NOT NULL, patron_id UUID NOT NULL, library_branch_id UUID NOT NULL, patron_books_database_entity INTEGER NOT NULL);
