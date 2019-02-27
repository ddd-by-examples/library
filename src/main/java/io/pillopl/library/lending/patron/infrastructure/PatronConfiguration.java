package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.dailysheet.model.DailySheet;
import io.pillopl.library.lending.patron.application.checkout.CollectingBookOnHold;
import io.pillopl.library.lending.patron.application.checkout.RegisteringOverdueCheckout;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.application.hold.ExpiringHolds;
import io.pillopl.library.lending.patron.application.hold.FindAvailableBook;
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold;
import io.pillopl.library.lending.patron.application.hold.HandleDuplicateHold;
import io.pillopl.library.lending.patron.application.hold.PlacingOnHold;
import io.pillopl.library.lending.patron.model.PatronBooksFactory;
import io.pillopl.library.lending.patron.model.PatronBooksRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@Configuration
@EnableJdbcRepositories
public class PatronConfiguration {

    @Bean
    CollectingBookOnHold collectingBookOnHold(FindBookOnHold findBookOnHold, PatronBooksRepository patronBooksRepository) {
        return new CollectingBookOnHold(findBookOnHold, patronBooksRepository);
    }

    @Bean
    RegisteringOverdueCheckout registeringOverdueCheckout(DailySheet dailySheet, PatronBooksRepository patronBooksRepository) {
        return new RegisteringOverdueCheckout(dailySheet, patronBooksRepository);
    }

    @Bean
    CancelingHold cancelingHold(FindBookOnHold findBookOnHold, PatronBooksRepository patronBooksRepository) {
        return new CancelingHold(findBookOnHold, patronBooksRepository);
    }

    @Bean
    ExpiringHolds expiringHolds(DailySheet dailySheet, PatronBooksRepository patronBooksRepository) {
        return new ExpiringHolds(dailySheet, patronBooksRepository);
    }

    @Bean
    HandleDuplicateHold handleDuplicateHold(CancelingHold cancelingHold) {
        return new HandleDuplicateHold(cancelingHold);
    }

    @Bean
    PlacingOnHold placingOnHold(FindAvailableBook findAvailableBook, PatronBooksRepository patronBooksRepository) {
        return new PlacingOnHold(findAvailableBook, patronBooksRepository);
    }

    @Bean
    PatronBooksRepository patronBooksRepository(PatronBooksEntityRepository patronBooksEntityRepository,
                                                DomainEvents domainEvents) {
        return new PatronBooksDatabaseRepository(
                patronBooksEntityRepository,
                new DomainModelMapper(new PatronBooksFactory()),
                domainEvents);
    }
}
