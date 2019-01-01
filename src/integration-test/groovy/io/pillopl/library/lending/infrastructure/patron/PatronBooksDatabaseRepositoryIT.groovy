package io.pillopl.library.lending.infrastructure.patron

import io.pillopl.library.lending.domain.book.Book
import io.pillopl.library.lending.domain.patron.PatronBooks
import io.pillopl.library.lending.domain.patron.PatronBooksEvent
import io.pillopl.library.lending.domain.patron.PatronBooksFixture
import io.pillopl.library.lending.domain.patron.PatronId
import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification

import static io.pillopl.library.lending.domain.book.BookFixture.circulatingBook

@ContextConfiguration(classes = TestDatabaseConfig.class)
@SpringBootTest
class PatronBooksDatabaseRepositoryIT extends Specification {

    PatronId patronId = PatronBooksFixture.anyPatronId()

    @Autowired
    PatronBooksDatabaseRepository patronResourcesRepository

    def 'persistence in real database should work'() {
        given:
            PatronBooks regular = PatronBooksFixture.regularPatron(patronId)
            Book book = circulatingBook()
        and:
            PatronBooksEvent.BookPlacedOnHold event = regular.placeOnHold(book).get()
        when:
            patronResourcesRepository.reactTo(event)
        then:
            PatronBooks patron = patronShouldBeFoundInDatabaseWithOneBookOnHold(patronId)
        when:
            PatronBooksEvent.BookCollected newEvent = patron.collect(book).get()
        and:
            patronResourcesRepository.reactTo(newEvent)
        then:
            patronShouldBeFoundInDatabaseWithZeroBooksOnHold(patronId)

    }

    PatronBooks patronShouldBeFoundInDatabaseWithOneBookOnHold(PatronId patronId) {
        PatronBooks patronBooks = loadPersistedPatron(patronId)
        assert patronBooks.numberOfHolds() == 1
        return patronBooks
    }

    PatronBooks patronShouldBeFoundInDatabaseWithZeroBooksOnHold(PatronId patronId) {
        PatronBooks patronBooks = loadPersistedPatron(patronId)
        assert patronBooks.numberOfHolds() == 0
        return patronBooks
    }

    PatronBooks loadPersistedPatron(PatronId patronId) {
        Option<PatronBooks> loaded = patronResourcesRepository.findBy(patronId)
        PatronBooks patronBooks = loaded.getOrElseThrow({
            new IllegalStateException("should have been persisted")
        })
        return patronBooks
    }
}
