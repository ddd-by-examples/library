package io.pillopl.library.lending.infrastructure.patron

import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronResources
import io.pillopl.library.lending.domain.patron.PatronResourcesFixture
import io.pillopl.library.lending.domain.resource.Resource
import io.pillopl.library.lending.infrastructure.patron.PatronResourcesDatabaseRepository
import io.pillopl.library.lending.infrastructure.patron.PatronResourcesEntityRepository
import io.pillopl.library.lending.infrastructure.patron.TestDatabaseConfig
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static io.pillopl.library.lending.domain.resource.ResourceFixture.circulatingResource

//TODO - move integration tests away of unit ones
@ContextConfiguration(classes = TestDatabaseConfig.class)
@SpringBootTest
class PatronResourcesDatabaseRepositoryTest extends Specification {

    final PatronId patronId = PatronResourcesFixture.anyPatronId()

    @Autowired
    PatronResourcesDatabaseRepository patronResourcesRepository

    @Autowired
    PatronResourcesEntityRepository patronResourcesEntityRepository

    def 'should bla bla'() {
        given:
            PatronResources regular = PatronResourcesFixture.regularPatron(patronId)
            Resource resource = circulatingResource()
        and:
            regular.placeOnHold(resource)
        when:
            patronResourcesRepository.save(regular)
        and:
            Option<PatronResources> loaded = patronResourcesRepository.findBy(patronId)
        then:
            PatronResources patronResources = loaded.getOrElseThrow({ new IllegalStateException("should have been persisted") })
            patronResources.toSnapshot().resourcesOnHold.size() == 1
        when:
            patronResources.collect(resource)
        and:
            patronResourcesRepository.save(patronResources)
        and:
            Option<PatronResources> againLoaded = patronResourcesRepository.findBy(patronId)
        then:
            PatronResources finalPatronResources = againLoaded.getOrElseThrow({ new IllegalStateException("should have been persisted") })
            finalPatronResources.toSnapshot().resourcesOnHold.size() == 0


    }
}
