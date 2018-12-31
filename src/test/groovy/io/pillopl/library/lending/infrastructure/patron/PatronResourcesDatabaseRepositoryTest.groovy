package io.pillopl.library.lending.infrastructure.patron

import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronResources
import io.pillopl.library.lending.domain.patron.PatronResourcesFixture
import io.pillopl.library.lending.domain.resource.Resource
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

    def 'should be able to map domain model to data model in the infrastructure layer'() {
        given:
            PatronResources regular = PatronResourcesFixture.regularPatron(patronId)
            Resource resource = circulatingResource()
        and:
            regular.placeOnHold(resource)
        when:
            patronResourcesRepository.save(regular)
        then:
            PatronResources patronResources = patronShouldBeFoundInDatabaseWithOneResourceOnHold(patronId)
        when:
            patronResources.collect(tusresource)
        and:
            patronResourcesRepository.save(patronResources)
        then:
            patronShouldBeFoundInDatabaseWithZeroResourceOnHold(patronId)

    }

    PatronResources patronShouldBeFoundInDatabaseWithOneResourceOnHold(PatronId patronId) {
        PatronResources patronResources = loadPersistedPatron(patronId)
        assert patronResources.toSnapshot().resourcesOnHold.size() == 1
        return patronResources
    }

    PatronResources patronShouldBeFoundInDatabaseWithZeroResourceOnHold(PatronId patronId) {
        PatronResources patronResources = loadPersistedPatron(patronId)
        assert patronResources.toSnapshot().resourcesOnHold.size() == 0
        return patronResources
    }

    PatronResources loadPersistedPatron(PatronId patronId) {
        Option<PatronResources> loaded = patronResourcesRepository.findBy(patronId)
        PatronResources patronResources = loaded.getOrElseThrow({
            new IllegalStateException("should have been persisted")
        })
        return patronResources
    }
}
