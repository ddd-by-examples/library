package io.pillopl.library.lending.dailysheet.infrastructure;

import io.pillopl.library.lending.LendingDatabaseConfig;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.time.Clock;

@Configuration
@Import(LendingDatabaseConfig.class)
public class SheetReadModelDatabaseConfiguration {

    @Bean
    DailySheet sheetReadModel(DataSource dataSource) {
        return new SheetsReadModel(new JdbcTemplate(dataSource), Clock.systemDefaultZone());
    }


}