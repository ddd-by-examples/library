package io.pillopl.library.lending.infrastructure.patron

import io.pillopl.library.lending.domain.patron.PatronId
import io.pillopl.library.lending.domain.patron.PatronResources
import io.pillopl.library.lending.domain.patron.PatronResourcesEvent
import io.pillopl.library.lending.domain.patron.PatronResourcesFixture
import io.pillopl.library.lending.domain.resource.Resource
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static io.pillopl.library.lending.domain.resource.ResourceFixture.circulatingResource

@ContextConfiguration(classes = TestDatabaseConfig.class)
@SpringBootTest
class PatronResourcesDatabaseRepositoryIT extends Specification {

    PatronId patronId = PatronResourcesFixture.anyPatronId()

    @Autowired
    PatronResourcesDatabaseRepository patronResourcesRepository

    @Autowired
    PatronResourcesEntityRepository patronResourcesEntityRepository

    def 'persistence in real database should work'() {
        given:
            PatronResources regular = PatronResourcesFixture.regularPatron(patronId)
            Resource resource = circulatingResource()
        and:
            PatronResourcesEvent.ResourcePlacedOnHold event = regular.placeOnHold(resource).get()
        when:
            patronResourcesRepository.reactTo(event)
        then:
            PatronResources patronResources =
                    patronShouldBeFoundInDatabaseWithOneResourceOnHold(patronId)
        when:
            PatronResourcesEvent.ResourceCollected newEvent = patronResources.collect(resource).get()
        and:
            patronResourcesRepository.reactTo(newEvent)
        then:
            patronShouldBeFoundInDatabaseWithZeroResourceOnHold(patronId)

    }

    PatronResources patronShouldBeFoundInDatabaseWithOneResourceOnHold(PatronId patronId) {
        PatronResources patronResources = loadPersistedPatron(patronId)
        assert patronResources.numberOfHolds() == 1
        return patronResources
    }

    PatronResources patronShouldBeFoundInDatabaseWithZeroResourceOnHold(PatronId patronId) {
        PatronResources patronResources = loadPersistedPatron(patronId)
        assert patronResources.numberOfHolds() == 0
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
