package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.lending.DatabaseConfig;
import io.pillopl.library.lending.book.application.PatronBookEventsHandler;
import io.pillopl.library.lending.book.model.BookRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Import(DatabaseConfig.class)
public class BookDatabaseConfiguration {

    @Bean
    BookRepository bookDatabaseRepository(DataSource dataSource) {
        return new BookDatabaseRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    PatronBookEventsHandler patronBookEventsHandler(BookRepository bookRepository) {
        return new PatronBookSpringEventsHandler(bookRepository);
    }

}