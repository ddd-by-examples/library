package io.pillopl.library.lending.domain.patron;

import io.vavr.control.Option;
import io.vavr.control.Try;

public interface PatronResourcesRepository {

    Option<PatronResources> findBy(PatronId patronId);

    Try<PatronResources> save(PatronResources patronResources);
}
