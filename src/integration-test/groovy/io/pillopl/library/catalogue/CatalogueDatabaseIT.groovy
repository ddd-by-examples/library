package io.pillopl.library.catalogue

import io.vavr.control.Option
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import static io.pillopl.library.catalogue.BookFixture.DDD
import static io.pillopl.library.catalogue.BookFixture.NON_PRESENT_ISBN
import static io.pillopl.library.catalogue.BookInstance.instanceOf
import static io.pillopl.library.catalogue.BookType.Restricted

@SpringBootTest(classes = CatalogueConfiguration.class)
class CatalogueDatabaseIT extends Specification {

    @Autowired
    CatalogueDatabase catalogueDatabase

    def 'should be able to save and load new book'() {
        given:
            Book book = DDD
        when:
            catalogueDatabase.saveNew(book)
        and:
            Option<Book> ddd = catalogueDatabase.findBy(book.bookIsbn)
        then:
            ddd.isDefined()
            ddd.get() == book
    }

    def 'should not load not present book'() {
        when:
            Option<Book> ddd = catalogueDatabase.findBy(NON_PRESENT_ISBN)
        then:
            ddd.isEmpty()
    }

    def 'should save book instance'() {
        when:
            catalogueDatabase.saveNew(instanceOf(DDD, Restricted))
        then:
            noExceptionThrown()
    }


}
