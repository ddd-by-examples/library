package io.pillopl.library.catalogue;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.commons.events.publisher.DomainEventsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@Import({CatalogueDatabaseConfig.class, DomainEventsConfig.class})
public class CatalogueConfiguration {

    @Bean
    Catalogue catalogue(CatalogueDatabase catalogueDatabase, DomainEvents domainEvents) {
        return new Catalogue(catalogueDatabase, domainEvents);
    }

    @Bean
    CatalogueDatabase catalogueDatabase(JdbcTemplate jdbcTemplate) {
        return new CatalogueDatabase(jdbcTemplate);
    }
}
