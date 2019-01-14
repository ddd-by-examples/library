package io.pillopl.library.lending.book.application;

import io.pillopl.library.lending.book.model.*;
import io.pillopl.library.lending.patron.model.PatronBooksEvent.*;
import io.vavr.API;
import lombok.AllArgsConstructor;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.Predicates.instanceOf;

@AllArgsConstructor
public class PatronBookEventsHandler {

    private final BookRepository bookRepository;

    public void handle(BookPlacedOnHold bookPlacedOnHold) {
        bookRepository.findBy(new BookId(bookPlacedOnHold.getBookId()))
                .map(book -> handleBookPlacedOnHold(book, bookPlacedOnHold))
                .forEach(bookRepository::save);
    }

    public void handle(BookCollected bookCollected) {
        bookRepository.findBy(new BookId(bookCollected.getBookId()))
                .map(book -> handleBookCollected(book, bookCollected))
                .forEach(bookRepository::save);
    }

    public void handle(BookHoldExpired holdExpired) {
        bookRepository.findBy(new BookId(holdExpired.getBookId()))
                .map(book -> handleBookHoldExpired(book, holdExpired))
                .forEach(bookRepository::save);
    }

    public void handle(BookHoldCanceled holdCanceled) {
        bookRepository.findBy(new BookId(holdCanceled.getBookId()))
                .map(book -> handleBookHoldCanceled(book,  holdCanceled))
                .forEach(bookRepository::save);
    }

    public void handle(BookReturned bookReturned) {
        bookRepository.findBy(new BookId(bookReturned.getBookId()))
                .map(book -> handleBookReturned(book, bookReturned))
                .forEach(bookRepository::save);
    }


    private Book handleBookPlacedOnHold(Book book, BookPlacedOnHold bookPlacedOnHold) {
        return API.Match(book).of(
                Case($(instanceOf(AvailableBook.class)), availableBook -> availableBook.handle(bookPlacedOnHold)),
                Case($(), () -> book)
        );
    }

    private Book handleBookHoldExpired(Book book, BookHoldExpired holdExpired) {
        return API.Match(book).of(
                Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(holdExpired)),
                Case($(), () -> book)
        );
    }

    private Book handleBookHoldCanceled(Book book, BookHoldCanceled holdCanceled) {
        return API.Match(book).of(
                Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(holdCanceled)),
                Case($(), () -> book)
        );
    }

    private Book handleBookCollected(Book book, BookCollected bookCollected) {
        return API.Match(book).of(
                Case($(instanceOf(BookOnHold.class)), onHold -> onHold.handle(bookCollected)),
                Case($(), () -> book)
        );
    }

    private Book handleBookReturned(Book book, BookReturned bookReturned) {
        return API.Match(book).of(
                Case($(instanceOf(CollectedBook.class)), collected -> collected.handle(bookReturned)),
                Case($(), () -> book)
        );
    }


}
