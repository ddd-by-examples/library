package io.pillopl.library.lending.infrastructure.book;

import io.pillopl.library.lending.application.hold.FindAvailableBook;
import io.pillopl.library.lending.domain.book.AvailableBook;
import io.pillopl.library.lending.domain.book.Book;
import io.pillopl.library.lending.domain.book.BookId;
import io.pillopl.library.lending.domain.book.BookRepository;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

import static io.vavr.API.*;
import static io.vavr.Patterns.$Some;
import static io.vavr.Predicates.instanceOf;

@AllArgsConstructor
class BookDatabaseRepository implements BookRepository, FindAvailableBook {

    private final BookEntityRepository bookEntityRepository;

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

    @Override
    public Option<AvailableBook> findAvailableBookBy(BookId bookId) {
        return Match(findBy(bookId)).of(
                Case($Some($(instanceOf(AvailableBook.class))), Option::of),
                Case($(), Option::none)

        );
    }
}

interface BookEntityRepository extends CrudRepository<BookDatabaseEntity, Long> {

    @Query("SELECT b.* FROM book_database_entity b where b.book_id = :bookId")
    Option<BookDatabaseEntity> findByBookId(@Param("bookId") UUID bookId);

}

