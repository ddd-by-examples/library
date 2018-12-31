package io.pillopl.library.lending.infrastructure.patron;

import io.pillopl.library.lending.domain.patron.PatronResourcesFactory;
import io.pillopl.library.lending.domain.patron.PatronResourcesRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.data.jdbc.repository.config.JdbcConfiguration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJdbcRepositories
class TestDatabaseConfig extends JdbcConfiguration {

    @Bean
    NamedParameterJdbcOperations operations() {
        return new NamedParameterJdbcTemplate(dataSource());
    }

    @Bean
    PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    DataSource dataSource(){
        return new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("createdb.sql")
                .build();
    }

    @Bean
    PatronResourcesRepository patronResourcesRepository(PatronResourcesEntityRepository patronResourcesEntityRepository) {
        return new PatronResourcesDatabaseRepository(patronResourcesEntityRepository, new PatronResourcesFactory());
    }
}