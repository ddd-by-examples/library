CREATE TABLE IF NOT EXISTS catalogue_book (
  id INTEGER IDENTITY PRIMARY KEY,
  isbn VARCHAR(100) NOT NULL,
  title VARCHAR(100) NOT NULL,
  author VARCHAR(100) NOT NULL);


CREATE TABLE IF NOT EXISTS catalogue_book_instance (
  id INTEGER IDENTITY PRIMARY KEY,
  isbn VARCHAR(100) NOT NULL,
  book_id UUID NOT NULL);

CREATE SEQUENCE catalogue_book_seq;
CREATE SEQUENCE catalogue_book_instance_seq;


