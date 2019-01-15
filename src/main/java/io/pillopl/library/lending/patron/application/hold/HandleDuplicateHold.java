package io.pillopl.library.lending.patron.application.hold;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.model.BookDuplicateHoldFound;
import io.pillopl.library.lending.book.model.BookId;
import io.pillopl.library.lending.library.model.LibraryBranchId;
import io.pillopl.library.lending.patron.model.PatronId;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;

import static java.time.Instant.now;

@AllArgsConstructor
public class HandleDuplicateHold {

    private final CancelingHold cancelingHold;

    @EventListener
    public Try<Result> handle(BookDuplicateHoldFound event) {
        return cancelingHold.cancelHold(cancelHoldCommandFrom(event));
    }

    private CancelHoldCommand cancelHoldCommandFrom(BookDuplicateHoldFound event) {
        return new CancelHoldCommand(
                now(),
                new PatronId(event.getSecondPatronId()),
                new LibraryBranchId(event.getLibraryBranchId()),
                new BookId(event.getBookId()));
    }

}

