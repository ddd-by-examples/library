package io.pillopl.library.lending.patronprofile.infrastructure;

import io.pillopl.library.lending.patronprofile.model.PatronProfiles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class PatronProfileConfiguration {

    @Bean
    public PatronProfiles patronProfilesReadModel(JdbcTemplate jdbcTemplate) {
        return new PatronProfileReadModel(jdbcTemplate);
    }
}
