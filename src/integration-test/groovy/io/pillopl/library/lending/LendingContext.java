package io.pillopl.library.lending;

import io.pillopl.library.lending.book.infrastructure.BookConfiguration;
import io.pillopl.library.lending.dailysheet.infrastructure.SheetReadModelDatabaseConfiguration;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.patron.application.hold.HandleDuplicateHold;
import io.pillopl.library.lending.patron.infrastructure.HandleDuplicateHoldWithSpringEvents;
import io.pillopl.library.lending.patron.infrastructure.PatronDatabaseConfigurationWithPersistedEvents;
import io.pillopl.library.lending.patron.model.PatronBooksRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({PatronDatabaseConfigurationWithPersistedEvents.class, BookConfiguration.class, SheetReadModelDatabaseConfiguration.class})
public class LendingContext {

    @Bean
    HandleDuplicateHold handleDuplicateHold(FindBookOnHold findBookOnHold, PatronBooksRepository patronBooksRepository) {
        return new HandleDuplicateHoldWithSpringEvents(new CancelingHold(findBookOnHold, patronBooksRepository));
    }

}