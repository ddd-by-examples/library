package io.pillopl.library.lending;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookInformation;
import io.pillopl.library.lending.book.model.BookRepository;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronEvent.PatronCreated;
import io.pillopl.library.lending.patron.model.PatronId;
import io.pillopl.library.lending.patron.model.Patrons;
import java.util.UUID;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;


import static io.pillopl.library.catalogue.BookType.Circulating;
import static io.pillopl.library.lending.patron.model.PatronType.Regular;

@Configuration
@Slf4j
class LendingDatabaseConfig extends AbstractJdbcConfiguration {

    @Bean
    JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }

    @Bean
    NamedParameterJdbcOperations operations() {
        return new NamedParameterJdbcTemplate(dataSource());
    }

    @Bean
    PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .addScript("create_patron_db.sql")
                .addScript("create_lending_book_db.sql")
                .addScript("create_sheets_db.sql")
                .build();
    }

    @Profile("local")
    @Bean
    CommandLineRunner init(BookRepository bookRepository, Patrons patrons) {
        return args -> {
            UUID bookId = UUID.randomUUID();
            UUID libraryBranchId = UUID.randomUUID();
            UUID patronId = UUID.randomUUID();

            AvailableBook availableBook = new AvailableBook(new BookInformation(new BookId(bookId), Circulating), new LibraryBranchId(libraryBranchId), new Version(0));
            bookRepository.save(availableBook);
            patrons.publish(PatronCreated.now(new PatronId(patronId), Regular));

            log.info("Created bookId: {}", bookId);
            log.info("Created libraryBranchId: {}", libraryBranchId);
            log.info("Created patronId: {}", patronId);
        };
    }
}
