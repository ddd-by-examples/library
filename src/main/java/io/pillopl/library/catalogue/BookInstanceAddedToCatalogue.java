package io.pillopl.library.catalogue;

import io.pillopl.library.commons.events.DomainEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class BookInstanceAddedToCatalogue implements DomainEvent {

    UUID eventId = UUID.randomUUID();
    String isbn;
    BookType type;
    UUID bookId;
    Instant when = Instant.now();

    BookInstanceAddedToCatalogue(BookInstance bookInstance) {
        this(bookInstance.getBookIsbn().getIsbn(), bookInstance.getBookType(), bookInstance.getBookId().getBookId());
    }

    @Override
    public UUID getAggregateId() {
        return bookId;
    }
}
