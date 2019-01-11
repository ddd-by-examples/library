package io.pillopl.library.lending.application.checkout

import io.pillopl.library.lending.application.hold.FindBookOnHold
import io.pillopl.library.lending.domain.book.BookOnHold
import io.pillopl.library.lending.domain.patron.PatronBooks
import io.pillopl.library.lending.domain.patron.PatronBooksRepository
import io.pillopl.library.lending.domain.patron.PatronId
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.application.checkout.CollectingBookOnHold.Result.Success
import static io.pillopl.library.lending.domain.book.BookFixture.anyBookId
import static io.pillopl.library.lending.domain.book.BookFixture.bookOnHold
import static io.pillopl.library.lending.domain.library.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.domain.patron.PatronBooksFixture.*

class CollectingBookOnHoldTest extends Specification {

    BookOnHold bookOnHold = bookOnHold()
    PatronId patronId = anyPatronId()

    FindBookOnHold willFindBook = { bookId, patronId -> Option.of(bookOnHold) }
    FindBookOnHold willNotFindBook = { bookId, patronId -> Option.none() }
    PatronBooksRepository repository = Stub()

    def 'should successfully collect book if patron and book exist'() {
        given:
            CollectingBookOnHold collecting = new CollectingBookOnHold(willFindBook, repository)
        and:
            persisted(regularPatronWith(bookOnHold, patronId))
        when:
            Try<CollectingBookOnHold.Result> result = collecting.collect(for3days(patronId))
        then:
            result.isSuccess()
            result.get() == Success

    }


    def 'should reject collecting if one of the domain rules is broken (but should not fail!)'() {
        given:
            CollectingBookOnHold collecting = new CollectingBookOnHold(willFindBook, repository)
        and:
            persisted(regularPatron(patronId))
        when:
            Try<CollectingBookOnHold.Result> result = collecting.collect(for3days(patronId))
        then:
            result.isSuccess()
            result.get() == CollectingBookOnHold.Result.Rejection

    }

    def 'should fail if patron does not exists'() {
        given:
            CollectingBookOnHold collecting = new CollectingBookOnHold(willFindBook, repository)
        and:
            unknownPatron()
        when:
            Try<CollectingBookOnHold.Result> result = collecting.collect(for3days(patronId))
        then:
            result.isFailure()

    }


    def 'should fail if book does not exists'() {
        given:
            CollectingBookOnHold collecting = new CollectingBookOnHold(willNotFindBook, repository)
        and:
            persisted(regularPatronWith(bookOnHold, patronId))
        when:
            Try<CollectingBookOnHold.Result> result = collecting.collect(for3days(patronId))
        then:
            result.isFailure()
    }

    def 'should reject (but not fail) if saving patron fails'() {
        given:
            CollectingBookOnHold collecting = new CollectingBookOnHold(willFindBook, repository)
        and:
            PatronId patron = persistedRegularPatronThatFailsOnSaving(patronId)
        when:
            Try<CollectingBookOnHold.Result> result = collecting.collect(for3days(patronId))
        then:
            result.isSuccess()
            result.get() == CollectingBookOnHold.Result.Rejection

    }

    CollectBookCommand for3days(PatronId patron) {
        return CollectBookCommand.create(patron, anyBranch(), anyBookId(), 4)
    }

    PatronId persisted(PatronBooks patron) {
        repository.findBy(patronId) >> Option.of(patron)
        repository.handle(_) >> Try.success(patron)
        return patronId
    }


    PatronId persistedRegularPatronThatFailsOnSaving(PatronId patronId) {
        PatronBooks patron = regularPatron(patronId)
        repository.findBy(patronId) >> Option.of(patron)
        repository.handle(_) >> Try.failure(new IllegalStateException())
        return patronId
    }

    PatronId unknownPatron() {
        repository.findBy(patronId) >> Option.none()
        return anyPatronId()
    }

}


