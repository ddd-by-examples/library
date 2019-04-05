package io.pillopl.library.lending.book.infrastructure;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.application.CreateAvailableBookOnInstanceAddedEventHandler;
import io.pillopl.library.lending.book.application.PatronEventsHandler;
import io.pillopl.library.lending.book.model.BookRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class BookConfiguration {

    @Bean
    CreateAvailableBookOnInstanceAddedEventHandler createAvailableBookOnInstanceAddedEventHandler(BookRepository bookRepository) {
        return new CreateAvailableBookOnInstanceAddedEventHandler(bookRepository);
    }

    @Bean
    PatronEventsHandler bookEventsHandler(BookRepository bookRepository, DomainEvents domainEvents) {
        return new PatronEventsHandler(bookRepository, domainEvents);
    }

    @Bean
    BookDatabaseRepository bookDatabaseRepository(JdbcTemplate jdbcTemplate) {
        return new BookDatabaseRepository(jdbcTemplate);
    }
}
