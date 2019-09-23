package io.pillopl.library.catalogue;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.commons.events.publisher.DomainEventsConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
@EnableAutoConfiguration
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

    @Profile("local")
    @Bean
    CommandLineRunner init(Catalogue catalogue) {
        return args -> {
            catalogue.addBook("Joshua Bloch", "Effective Java", "0321125215").get();
            catalogue.addBookInstance("0321125215", BookType.Restricted).get();
        };
    }
}
