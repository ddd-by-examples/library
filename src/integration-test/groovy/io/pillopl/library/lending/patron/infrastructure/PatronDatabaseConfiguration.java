package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.commons.events.publisher.JustForwardDomainEventPublisher;
import io.pillopl.library.lending.LendingDatabaseConfig;
import io.pillopl.library.lending.patron.model.PatronBooksFactory;
import io.pillopl.library.lending.patron.model.PatronBooksRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@EnableJdbcRepositories
@Import(LendingDatabaseConfig.class)
public
class PatronDatabaseConfiguration {

    @Bean
    PatronBooksRepository patronBooksRepository(PatronBooksEntityRepository patronBooksEntityRepository,
                                                ApplicationEventPublisher applicationEventPublisher) {
        return new PatronBooksDatabaseRepository(
                patronBooksEntityRepository,
                new DomainModelMapper(new PatronBooksFactory()),
                new JustForwardDomainEventPublisher(applicationEventPublisher));
    }


}