package io.pillopl.library.lending.patron.application.checkout


import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.patron.application.hold.FindBookOnHold
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.patron.model.Patron
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.Patrons
import io.pillopl.library.lending.patron.model.PatronId
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.bookOnHold
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.*

class CheckingOutBookOnHoldTest extends Specification {

    BookOnHold bookOnHold = bookOnHold()
    PatronId patronId = anyPatronId()

    FindBookOnHold willFindBook = { bookId, patronId -> Option.of(bookOnHold) }
    FindBookOnHold willNotFindBook = { bookId, patronId -> Option.none() }
    Patrons repository = Stub()

    def 'should successfully check out book if patron and book exist'() {
        given:
		CheckingOutBookOnHold checkingOut = new CheckingOutBookOnHold(willFindBook, repository)
        and:
            persisted(regularPatronWith(bookOnHold, patronId))
        when:
            Try<Result> result = checkingOut.checkOut(for3days(patronId))
        then:
            result.isSuccess()
            result.get() == Result.Success

    }

    def 'should reject checking out if one of the domain rules is broken (but should not fail!)'() {
        given:
		CheckingOutBookOnHold checkingOut = new CheckingOutBookOnHold(willFindBook, repository)
        and:
            persisted(regularPatron(patronId))
        when:
            Try<Result> result = checkingOut.checkOut(for3days(patronId))
        then:
            result.isSuccess()
            result.get() == Result.Rejection

    }

    def 'should fail if patron does not exists'() {
        given:
		CheckingOutBookOnHold checkingOut = new CheckingOutBookOnHold(willFindBook, repository)
        and:
            unknownPatron()
        when:
            Try<Result> result = checkingOut.checkOut(for3days(patronId))
        then:
            result.isFailure()

    }


    def 'should fail if book does not exists'() {
        given:
		CheckingOutBookOnHold checkingOut = new CheckingOutBookOnHold(willNotFindBook, repository)
        and:
            persisted(regularPatronWith(bookOnHold, patronId))
        when:
            Try<Result> result = checkingOut.checkOut(for3days(patronId))
        then:
            result.isFailure()
    }

    def 'should fail if saving patron fails'() {
        given:
            CheckingOutBookOnHold checkingOutBookOnHold = new CheckingOutBookOnHold(willFindBook, repository)
        and:
            PatronId patron = persistedRegularPatronThatFailsOnSaving(patronId)
        when:
            Try<Result> result = checkingOutBookOnHold.checkOut(for3days(patronId))
        then:
            result.isFailure()

    }

    CheckOutBookCommand for3days(PatronId patron) {
        return CheckOutBookCommand.create(patron, anyBranch(), anyBookId(), 4)
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


