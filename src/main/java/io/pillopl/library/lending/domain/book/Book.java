package io.pillopl.library.lending.domain.book;

import io.pillopl.library.lending.domain.library.LibraryBranchId;
import io.pillopl.library.lending.domain.patron.PatronBooksEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static io.pillopl.library.lending.domain.book.Book.BookState.COLLECTED;
import static io.pillopl.library.lending.domain.book.Book.BookState.ON_HOLD;

@AllArgsConstructor
public class Book {

    enum BookState {AVAILABLE, ON_HOLD, COLLECTED}

    enum BookType {RESTRICTED, CIRCULATING}

    @Getter
    private final BookId bookId;

    @Getter
    private final LibraryBranchId libraryBranch;

    private final BookType type;

    private BookState state;

    public boolean isRestricted() {
        return type.equals(BookType.RESTRICTED);
    }

    public boolean isAvailable() {
        return state.equals(BookState.AVAILABLE);
    }

    //TODO events upper
    public void handle(PatronBooksEvent.BookReturned event) {
        this.state = BookState.AVAILABLE;
    }

    public void handle(PatronBooksEvent.BookPlacedOnHold event) {
        state = ON_HOLD;
    }

    public void handle(PatronBooksEvent.BookCollected event) {
        state = COLLECTED;
    }
}

