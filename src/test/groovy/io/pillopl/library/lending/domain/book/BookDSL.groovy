package io.pillopl.library.lending.domain.book


import static io.pillopl.library.lending.domain.book.BookType.Circulating

class BookDSL {

    static BookFixt the(BookFixt book) {
        book
    }

    static BookFixt an(BookFixt book) {
        book
    }

    static BookFixt aCirculatingBook() {
        new BookFixt(Circulating)
    }

}
