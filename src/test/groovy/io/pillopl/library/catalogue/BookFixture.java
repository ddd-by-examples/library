package io.pillopl.library.catalogue;

class BookFixture {

    static final String DDD_ISBN_STR = "0321125215";

    static final ISBN DDD_ISBN_10 = new ISBN(DDD_ISBN_STR);

    static final ISBN NON_PRESENT_ISBN = new ISBN("032112521X");

    static final Book DDD = new Book(new ISBN(DDD_ISBN_STR), new Title("DDD"), new Author("Eric Evans"));
}
