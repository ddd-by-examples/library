package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.common.events.publisher.DomainEventsConfig;
import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.DatabaseConfig;
import io.pillopl.library.lending.patron.model.PatronBooksFactory;
import io.pillopl.library.lending.patron.model.PatronBooksRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableJdbcRepositories
@Import({DatabaseConfig.class, DomainEventsConfig.class})
@EnableScheduling
public class PatronDatabaseConfigurationWithPersistedEvents {

    @Bean
    PatronBooksRepository patronBooksRepository(PatronBooksEntityRepository patronBooksEntityRepository, DomainEvents domainEvents) {
        return new PatronBooksDatabaseRepository(
                patronBooksEntityRepository,
                new DomainModelMapper(new PatronBooksFactory()),
                domainEvents);
    }

}