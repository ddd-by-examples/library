package io.pillopl.library.lending;

import io.pillopl.library.commons.events.publisher.DomainEventsConfig;
import io.pillopl.library.lending.book.infrastructure.BookConfiguration;
import io.pillopl.library.lending.dailysheet.infrastructure.DailySheetConfiguration;
import io.pillopl.library.lending.patron.infrastructure.PatronConfiguration;
import io.pillopl.library.lending.patronprofile.infrastructure.PatronProfileConfiguration;
import io.pillopl.library.lending.patronprofile.web.WebConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@Import({LendingDatabaseConfig.class,
        WebConfiguration.class,
        PatronProfileConfiguration.class,
        PatronConfiguration.class,
        DailySheetConfiguration.class,
        BookConfiguration.class,
        DomainEventsConfig.class})
public class LendingConfig {
}
