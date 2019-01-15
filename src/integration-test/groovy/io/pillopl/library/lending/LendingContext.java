package io.pillopl.library.lending;

import io.pillopl.library.lending.book.infrastructure.BookConfiguration;
import io.pillopl.library.lending.dailysheet.infrastructure.SheetReadModelDatabaseConfiguration;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.patron.application.hold.HandleDuplicateHold;
import io.pillopl.library.lending.patron.infrastructure.PatronConfigurationWithPersistedEvents;
import io.pillopl.library.lending.patron.model.PatronBooksRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({PatronConfigurationWithPersistedEvents.class, BookConfiguration.class, SheetReadModelDatabaseConfiguration.class})
public class LendingContext {

    @Bean
    HandleDuplicateHold handleDuplicateHold(FindBookOnHold findBookOnHold, PatronBooksRepository patronBooksRepository) {
        return new HandleDuplicateHold(new CancelingHold(findBookOnHold, patronBooksRepository));
    }

}