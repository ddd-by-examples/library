package io.pillopl.library.lending;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({LendingConfig.class})
public class LendingTestContext {
}