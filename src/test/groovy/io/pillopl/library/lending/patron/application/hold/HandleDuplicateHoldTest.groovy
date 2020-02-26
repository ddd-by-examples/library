package io.pillopl.library.lending.patron.application.hold

import io.pillopl.library.commons.commands.Result
import io.pillopl.library.lending.book.model.BookDuplicateHoldFound
import io.pillopl.library.lending.book.model.BookOnHold
import io.pillopl.library.lending.patron.model.Patron
import io.pillopl.library.lending.patron.model.PatronEvent
import io.pillopl.library.lending.patron.model.PatronFixture
import io.pillopl.library.lending.patron.model.PatronId
import io.pillopl.library.lending.patron.model.Patrons
import io.vavr.control.Option
import io.vavr.control.Try
import spock.lang.Specification

import static io.pillopl.library.lending.book.model.BookFixture.anyBookId
import static io.pillopl.library.lending.book.model.BookFixture.bookOnHold
import static io.pillopl.library.lending.librarybranch.model.LibraryBranchFixture.anyBranch
import static io.pillopl.library.lending.patron.model.PatronFixture.anyPatronId
import static io.pillopl.library.lending.patron.model.PatronFixture.regularPatron
import static java.time.Instant.now

class HandleDuplicateHoldTest extends Specification {

    BookOnHold bookOnHold = bookOnHold()
    PatronId patronId = anyPatronId()

    FindBookOnHold willFindBook = { bookId, patronId -> Option.of(bookOnHold) }
    Patrons repository = Stub()

    def "should successfully cancel hold if book was hold by the patron"() {
        given:
            CancelingHold cancelingHold = new CancelingHold(willFindBook, repository)
            HandleDuplicateHold duplicateHold = new HandleDuplicateHold(cancelingHold)
        and:
            persistedRegularPatronWithBookOnHold()
        when:
            Try<Result> result = duplicateHold.handle(duplicateHoldFoundBy(patronId))
        then:
            result.isSuccess()
            result.get() == Result.Success
    }

    def "should reject cancelling hold if book was not hold by the patron"() {
        given:
            CancelingHold cancelingHold = new CancelingHold(willFindBook, repository)
            HandleDuplicateHold duplicateHold = new HandleDuplicateHold(cancelingHold)
        and:
            persistedRegularPatron()
        when:
            Try<Result> result = duplicateHold.handle(duplicateHoldFoundBy(patronId))
        then:
            result.isSuccess()
            result.get() == Result.Rejection
    }

    PatronId persistedRegularPatronWithBookOnHold() {
        Patron patron = PatronFixture.regularPatronWithHold(bookOnHold)
        repository.findBy(patronId) >> Option.of(patron)
        repository.publish(_ as PatronEvent) >> patron
        return patronId
    }

    PatronId persistedRegularPatron() {
        Patron patron = regularPatron(patronId)
        repository.findBy(patronId) >> Option.of(patron)
        repository.publish(_ as PatronEvent) >> patron
        return patronId
    }

    BookDuplicateHoldFound duplicateHoldFoundBy(PatronId patron) {
        return new BookDuplicateHoldFound(
                now(),
                anyPatronId().patronId,
                patron.patronId,
                anyBranch().libraryBranchId,
                anyBookId().bookId
        )
    }

}
