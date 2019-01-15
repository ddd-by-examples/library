package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.common.events.publisher.DomainEventsConfig;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.DatabaseConfig;
import io.pillopl.library.lending.book.application.PatronBookEventsHandler;
import io.pillopl.library.lending.book.model.BookRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Import({DatabaseConfig.class, DomainEventsConfig.class})
public class BookConfiguration {

    @Bean
    BookRepository bookDatabaseRepository(DataSource dataSource) {
        return new BookDatabaseRepository(new JdbcTemplate(dataSource));
    }

    @Bean
    PatronBookEventsHandler patronBookEventsHandler(BookRepository bookRepository, DomainEvents events) {
        return new PatronBookEventsHandler(bookRepository, events);
    }



}