package io.pillopl.library.lending.book.infrastructure;


import io.pillopl.library.commons.events.DomainEvents;
import io.pillopl.library.lending.book.application.PatronBookEventsHandler;
import io.pillopl.library.lending.book.model.BookRepository;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.*;
import org.springframework.context.event.EventListener;

class PatronBookSpringEventsHandler extends PatronBookEventsHandler {

    PatronBookSpringEventsHandler(BookRepository bookRepository, DomainEvents domainEvents) {
        super(bookRepository, domainEvents);
    }

    @EventListener
    public void handle(BookPlacedOnHold bookPlacedOnHold) {
        super.handle(bookPlacedOnHold);
    }

    @EventListener
    public void handle(BookHoldExpired bookHoldExpired) {
        super.handle(bookHoldExpired);
    }

    @EventListener
    public void handle(BookHoldCanceled bookHoldCanceled) {
        super.handle(bookHoldCanceled);
    }

    @EventListener
    public void handle(BookCollected bookCollected) {
        super.handle(bookCollected);
    }

    @EventListener
    public void handle(BookReturned bookReturned) {
        super.handle(bookReturned);
    }

}
