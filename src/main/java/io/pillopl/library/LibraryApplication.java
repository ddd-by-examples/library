package io.pillopl.library;

import io.pillopl.library.catalogue.CatalogueConfiguration;
import io.pillopl.library.lending.LendingConfig;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = DataSourceAutoConfiguration.class)
public class LibraryApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .parent(LibraryApplication.class)
                .child(LendingConfig.class).web(WebApplicationType.SERVLET)
                .sibling(CatalogueConfiguration.class).web(WebApplicationType.NONE)
                .run(args);
    }
}
