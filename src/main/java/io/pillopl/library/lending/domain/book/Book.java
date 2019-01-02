package io.pillopl.library.lending.domain.book;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static io.pillopl.library.lending.domain.book.Book.BookState.Available;
import static io.pillopl.library.lending.domain.book.Book.BookState.Collected;
import static io.pillopl.library.lending.domain.book.Book.BookState.OnHold;
import static io.pillopl.library.lending.domain.book.Book.BookType.Restricted;

@AllArgsConstructor
public class Book {

    enum BookState {Available, OnHold, Collected}

    enum BookType {Restricted, Circulating}

    @Getter
    private final BookId bookId;

    @Getter
    private final LibraryBranchId libraryBranch;

    private final BookType type;

    private BookState state;

    public boolean isRestricted() {
        return type.equals(Restricted);
    }

    public boolean isAvailable() {
        return state.equals(Available);
    }

    //TODO events upper
    public void handle(PatronBooksEvent.BookReturned event) {
        this.state = Available;
    }

    public void handle(PatronBooksEvent.BookPlacedOnHold event) {
        state = OnHold;
    }

    public void handle(PatronBooksEvent.BookCollected event) {
        state = Collected;
    }
}

