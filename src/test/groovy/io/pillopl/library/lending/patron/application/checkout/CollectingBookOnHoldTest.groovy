package io.pillopl.library.lending.patron.application.checkout


import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.patron.model.Patron
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronRepository
import io.pillopl.library.lending.patron.model.PatronId
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.bookOnHold
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.*

class CollectingBookOnHoldTest extends Specification {

    BookOnHold bookOnHold = bookOnHold()
    PatronId patronId = anyPatronId()

    FindBookOnHold willFindBook = { bookId, patronId -> Option.of(bookOnHold) }
    FindBookOnHold willNotFindBook = { bookId, patronId -> Option.none() }
    PatronRepository repository = Stub()

    def 'should successfully collect book if patron and book exist'() {
        given:
            CollectingBookOnHold collecting = new CollectingBookOnHold(willFindBook, repository)
        and:
            persisted(regularPatronWith(bookOnHold, patronId))
        when:
            Try<Result> result = collecting.collect(for3days(patronId))
        then:
            result.isSuccess()
            result.get() == Result.Success

    }

    def 'should reject collecting if one of the domain rules is broken (but should not fail!)'() {
        given:
            CollectingBookOnHold collecting = new CollectingBookOnHold(willFindBook, repository)
        and:
            persisted(regularPatron(patronId))
        when:
            Try<Result> result = collecting.collect(for3days(patronId))
        then:
            result.isSuccess()
            result.get() == Result.Rejection

    }

    def 'should fail if patron does not exists'() {
        given:
            CollectingBookOnHold collecting = new CollectingBookOnHold(willFindBook, repository)
        and:
            unknownPatron()
        when:
            Try<Result> result = collecting.collect(for3days(patronId))
        then:
            result.isFailure()

    }


    def 'should fail if book does not exists'() {
        given:
            CollectingBookOnHold collecting = new CollectingBookOnHold(willNotFindBook, repository)
        and:
            persisted(regularPatronWith(bookOnHold, patronId))
        when:
            Try<Result> result = collecting.collect(for3days(patronId))
        then:
            result.isFailure()
    }

    def 'should fail if saving patron fails'() {
        given:
            CollectingBookOnHold collecting = new CollectingBookOnHold(willFindBook, repository)
        and:
            PatronId patron = persistedRegularPatronThatFailsOnSaving(patronId)
        when:
            Try<Result> result = collecting.collect(for3days(patronId))
        then:
            result.isFailure()

    }

    CollectBookCommand for3days(PatronId patron) {
        return CollectBookCommand.create(patron, anyBranch(), anyBookId(), 4)
    }

    PatronId persisted(Patron patron) {
        repository.findBy(patronId) >> Option.of(patron)
        repository.publish(_ as PatronEvent) >> patron
        return patronId
    }


    PatronId persistedRegularPatronThatFailsOnSaving(PatronId patronId) {
        Patron patron = regularPatron(patronId)
        repository.findBy(patronId) >> Option.of(patron)
        repository.publish(_ as PatronEvent) >> {throw new IllegalStateException()}
        return patronId
    }

    PatronId unknownPatron() {
        repository.findBy(patronId) >> Option.none()
        return anyPatronId()
    }

}


