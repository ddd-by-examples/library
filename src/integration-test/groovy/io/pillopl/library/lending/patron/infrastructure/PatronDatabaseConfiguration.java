package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.commons.events.AtMostOnceDomainEventPublisher;
import io.pillopl.library.lending.DatabaseConfig;
import io.pillopl.library.lending.patron.infrastructure.DomainModelMapper;
import io.pillopl.library.lending.patron.infrastructure.PatronBooksDatabaseRepository;
import io.pillopl.library.lending.patron.infrastructure.PatronBooksEntityRepository;
import io.pillopl.library.lending.patron.model.PatronBooksFactory;
import io.pillopl.library.lending.patron.model.PatronBooksRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@EnableJdbcRepositories
@Import(DatabaseConfig.class)
public
class PatronDatabaseConfiguration {

    @Bean
    PatronBooksRepository patronBooksRepository(PatronBooksEntityRepository patronBooksEntityRepository,
                                                ApplicationEventPublisher applicationEventPublisher) {
        return new PatronBooksDatabaseRepository(
                patronBooksEntityRepository,
                new DomainModelMapper(new PatronBooksFactory()),
                new AtMostOnceDomainEventPublisher(applicationEventPublisher));
    }


}