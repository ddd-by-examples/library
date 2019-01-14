package io.pillopl.library.lending.patron.infrastructure;

import io.pillopl.library.commons.commands.Result;
import io.pillopl.library.lending.book.model.BookDuplicateHoldFound;
import io.pillopl.library.lending.patron.application.hold.CancelingHold;
import io.pillopl.library.lending.patron.application.hold.HandleDuplicateHold;
import io.vavr.control.Try;
import org.springframework.context.event.EventListener;

public class HandleDuplicateHoldWithSpringEvents extends HandleDuplicateHold {

    public HandleDuplicateHoldWithSpringEvents(CancelingHold cancelingHold) {
        super(cancelingHold);
    }

    @EventListener
    public Try<Result> handle(BookDuplicateHoldFound event) {
        return super.handle(event);
    }

}