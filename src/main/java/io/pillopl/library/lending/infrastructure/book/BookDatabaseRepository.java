package io.pillopl.library.lending.infrastructure.book;

import io.pillopl.library.lending.domain.book.Book;
import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.book.BookRepository;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

class BookDatabaseRepository implements BookRepository {

    private final BookEntityRepository bookEntityRepository;

    BookDatabaseRepository(BookEntityRepository bookEntityRepository) {
        this.bookEntityRepository = bookEntityRepository;
    }

    @Override
    public Option<Book> findBy(BookId bookId) {
        return bookEntityRepository.findByBookId(bookId.getBookId())
                .map(BookDatabaseEntity::toDomainModel);
    }

    @Override
    public Try<Void> save(Book book) {
        return Try.run(() -> {
            BookDatabaseEntity foundBook = bookEntityRepository
                    .findByBookId(book.bookId().getBookId())
                    .map(entity -> entity.updateFromDomainModel(book))
                    .getOrElse(() -> createNewBook(book));
            bookEntityRepository.save(foundBook);
        });
    }

    private BookDatabaseEntity createNewBook(Book book) {
        return BookDatabaseEntity.from(book);
    }

}

interface BookEntityRepository extends CrudRepository<BookDatabaseEntity, Long> {

    @Query("SELECT b.* FROM book_database_entity b where b.book_id = :bookId")
    Option<BookDatabaseEntity> findByBookId(@Param("bookId") UUID bookId);

}

