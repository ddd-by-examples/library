package io.pillopl.library.lending.dailysheet.infrastructure;

import io.pillopl.library.lending.dailysheet.model.DailySheet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Clock;

@Configuration
public class DailySheetConfiguration {

    @Bean
    DailySheet sheetsReadModel(JdbcTemplate jdbcTemplate) {
        return new SheetsReadModel(jdbcTemplate, Clock.systemDefaultZone());
    }
}
