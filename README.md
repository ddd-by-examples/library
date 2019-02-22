# Table of contents

1. [About](#about)
2. [Domain description](#domain)
3. [General assumptions](#general-assumptions)  
    3.1 [Process discovery](#process-discovery)  
    3.2 [Bounded Contexts](#bounded-contexts)  
    3.3 [ArchUnit](#archunit)  
    3.4 [Functional thinking](#functional-thinking)  
    3.5 [No ORM](#no-orm)  
    3.6 [Architecture-code gap](#architecture-code-gap)  
    3.7 [Model-code gap](#model-code-gap)  
    3.8 [HATEOAS](#hateoas)  
    3.9 [Test DSL](#test-dsl)  
4. [How to contribute](#how-to-contribute)

## About

This is a project of a library, driven by real [business requirements](#domain-description).
We use techniques strongly connected with Domain Driven Design, Behavior-Driven Development,
Event Storming, User Story Mapping. 

## Domain description

A public library allows patrons to place books on hold at its various library branches.
Available books can be placed on hold only by one patron at any given point in time.
Books are either circulating or restricted, and can have retrieval or usage fees.
A restricted book can only be held by a researcher patron. A regular patron is limited
to five holds at any given moment, while a researcher patron is allowed an unlimited number
of holds. An open-ended book hold is active until the patron collects the book, at which time it
is completed. A closed-ended book hold that is not completed within a fixed number of 
days after it was requested will expire. This check is done at the beginning of a day by 
taking a look at daily sheet with expiring holds. Only a researcher patron can request
an open-ended hold duration. Any patron with more than two overdue checkouts at a library
branch will get a rejection if trying a hold at that same library branch. A book can be
checked out for up to 60 days. Check for overdue checkouts is done by taking a look at
daily sheet with overdue checkouts. Patron interacts with his/her current holds, checkouts, etc.
by taking a look at patron profile. Patron profile looks like a daily sheet, but the
information there is limited to one patron and is not necessarily daily. Currently a
patron can see current holds (not canceled nor expired) and current checkouts (including overdue).
Also, he/she is able to hold a book and cancel a hold.

How actually a patron knows which books are there to lend? Library has its catalogue of
books where books are added together with their specific instances. A specific book
instance of a book can be added only if there is book with matching ISBN already in
the catalogue.  Book must have non-empty title and price. At the time of adding an instance
we decide whether it will be Circulating or Restricted. This enables
us to have book with same ISBN as circulated and restricted at the same time (for instance,
there is a book signed by the author that we want to keep as Restricted)

## General assumptions

### Process discovery

The first thing we started with was domain exploration with the help of Big Picture EventStorming.
During the session we discovered following [definitions](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346371442125):
* [Patron](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346371442075)
* [Available Book](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346399152354)
* [Circulating Book](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346399152400)
* [Restricted Book](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346399152464)
* [Book On Hold](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346457917238)
* [Collected Book](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346457964937)
* [Catalogue](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346457973874)
* [Hold Duration](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346457974002)
* [Expired Hold](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346457974040)
* [Checkout](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346457974057)
* [Overdue Checkout](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346457983753)
* [Daily Sheet With Expired Holds](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346457983847)
* [Daily Sheet With Overdue Checkouts](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346457999229)

### Bounded Contexts
In order to properly identify bounded contexts we conducted the Design Level Event Storming that
was based on the results of Example Mapping, where each case (example) was modelled.

Heuristics:
* Linguistic boundaries
* Data: flow, ownership, uniqueness
* Domain expert boundaries
* Existing organisational boundaries
* Business process steps
* Transaction boundaries

TODO

### Project structure
At the very beginning, not to overcomplicate the project, we decided to assign each bounded context
to a separate package, which means that the system is a modular monolith.  There are no obstacles, though,
to put contexts into maven modules or finally to microservices.

Bounded contexts should (amongst others) introduce autonomy in the sense of architecture. Thus, each module
encapsulating the context has its own local architecture aligned to problem complexity.
In the case of a context, where we identified true business logic (**lending**) we introduced a domain model
that is a simplified (for the purpose of the project) abstraction of the reality.

If we are talking about architecture, the hexagon lets us separate domain and application logic from
frameworks (and infrastructure). What do we gain with this approach? Firstly, we can unit test most important
part of the application - **business logic** - usually without the need to stub any dependency.

Spring...

CQRS...


### ArchUnit

One of the main components of a successful project is technical leadership that lets the team go in the right
direction. Nevertheless, there are tools that can support teams in keeping the code clean and protect the
architecture, so that the project won't become a Big Ball of Mud, and thus will be pleasant to develop and
to maintain. The first option, the one we proposed, is [ArchUnit](https://www.archunit.org/) - a Java architecture
test tool. ArchUnit lets you write unit tests of your architecture, so that it is always consistent with initial
vision. Maven modules could be an alternative as well, but let's focus on the former.

In terms of hexagonal architecture, it is essential to ensure, that we do not mix different levels of
abstraction (hexagon levels):
```java 
@ArchTest
public static final ArchRule model_should_not_depend_on_infrastructure =
    noClasses()
        .that()
        .resideInAPackage("..model..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("..infrastructure..");
```      
and that frameworks do not affect the domain model  
```java
@ArchTest
public static final ArchRule model_should_not_depend_on_spring =
    noClasses()
        .that()
        .resideInAPackage("..io.pillopl.library.lending..model..")
        .should()
        .dependOnClassesThat()
        .resideInAPackage("org.springframework..");
```    

### Functional thinking
When you look at the code you might find a scent of functional programming. Although we do not follow
a _clean_ FP, we try to think of business processes as pipelines or workflows, utilizing functional style through
following concepts.

_Please note that this is not a reference project for FP._

#### Immutable objects
Each class that represents a business concept is immutable, thanks to which we:
* provide full encapsulation and objects' states protection,
* secure objects for multithreaded access,
* control all side effects much clearer. 

#### Pure functions
Modelling domain operations, discovered in Design Level Event Storming, as pure functions, and declaring them in
both domain and application layers in the form of Java's functional interfaces. Their implementations are placed
in infrastructure layer as ordinary methods with side effects. Thanks to this approach we can follow the abstraction
of ubiquitous language explicitly, and keep this abstraction implementation-agnostic. As an example, you could have
a look at `FindAvailableBook` interface and its implementation:

```java
@FunctionalInterface
public interface FindAvailableBook {

    Option<AvailableBook> findAvailableBookBy(BookId bookId);
}
```

```java
@AllArgsConstructor
class BookDatabaseRepository implements FindAvailableBook {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Option<AvailableBook> findAvailableBookBy(BookId bookId) {
        return Match(findBy(bookId)).of(
                Case($Some($(instanceOf(AvailableBook.class))), Option::of),
                Case($(), Option::none)
        );
    }  

    Option<Book> findBy(BookId bookId) {
        return findBookById(bookId)
                .map(BookDatabaseEntity::toDomainModel);
    }

    private Option<BookDatabaseEntity> findBookById(BookId bookId) {
        return Try
                .ofSupplier(() -> of(jdbcTemplate.queryForObject("SELECT b.* FROM book_database_entity b WHERE b.book_id = ?",
                                      new BeanPropertyRowMapper<>(BookDatabaseEntity.class), bookId.getBookId())))
                .getOrElse(none());
    }  
} 
```
    
#### Type system
_Type system - like_ modelling - we modelled each domain object's state discovered during EventStorming as separate
classes: `AvailableBook`, `BookOnHold`, `CollectedBook`. With this approach we provide much clearer abstraction than
having a single `Book` class with an enum-based state management. Moving the logic to these specific classes brings
Single Responsibility Principle to a different level. Moreover, instead of checking invariants in every business method
we leave the role to the compiler. As an example, please consider following scenario: _you can place on hold only a book
that is currently available_. We could have done it in a following way:
```java
public Either<BookHoldFailed, BookPlacedOnHoldEvents> placeOnHold(Book book) {
  if (book.status == AVAILABLE) {  
      ...
  }
}
```
but we use the _type system_ and declare method of following signature
```java
public Either<BookHoldFailed, BookPlacedOnHoldEvents> placeOnHold(AvailableBook book) {
      ...
}
```  
The more errors we discover at compile time the better.

#### Monads
Business methods might have different results. One might return a value or a `null`, throw an exception when something
unexpected happens or just return different objects under different circumstances. All those situations are typical
to object-oriented languages like Java, but do not fit into functional style. We are dealing with this issues
with monads (monadic containers provided by [Vavr](https://www.vavr.io)):
* When a method returns optional value, we use the `Option` monad:

    ```java
    Option<Book> findBy(BookId bookId) {
        ...
    }
    ```

* When a method might return one of two possible values, we use the `Either` monad:

    ```java
    Either<BookHoldFailed, BookPlacedOnHoldEvents> placeOnHold(AvailableBook book) {
        ...
    }
    ```

* When an exception might occur, we use `Try` monad:

    ```java
    Try<Result> placeOnHold(@NonNull PlaceOnHoldCommand command) {
        ...
    }
    ```

Thanks to this, we can follow the functional programming style, but we also enrich our domain language and
make our code much more readable for the clients.

#### Pattern Matching
Depending on a type of a given book object we often need to perform different actions. Series of if/else or switch/case statements
could be a choice, but it is the pattern matching that provides the most conciseness and flexibility. With the code
like below we can check numerous patterns against objects and access their constituents, so our code has a minimal dose
of language-construct noise:
```java
private Book handleBookPlacedOnHold(Book book, BookPlacedOnHold bookPlacedOnHold) {
    return API.Match(book).of(
        Case($(instanceOf(AvailableBook.class)), availableBook -> availableBook.handle(bookPlacedOnHold)),
        Case($(instanceOf(BookOnHold.class)), bookOnHold -> raiseDuplicateHoldFoundEvent(bookOnHold, bookPlacedOnHold)),
        Case($(), () -> book)
    );
}
```

### (No) ORM
If you run `mvn dependency:tree` you won't find any JPA implementation. Although we think that ORM solutions (like Hibernate)
are very powerful and useful, we decided not to use them, as we wouldn't utilize their features. What features are
talking about? Lazy loading, caching, dirty checking. Why don't we need them? We want to have more control
over SQL queries and minimize the object-relational impedance mismatch ourselves. Moreover, thanks to relatively
small aggregates, containing as little data as it is required to protect the invariants, we don't need the
lazy loading mechanism either.
With Hexagonal Architecture we have the ability to separate domain and persistence models and test them independently.
In the infrastructure layer we use new and very promising project called Spring Data JDBC, that is free from
the JPA-related overhead mentioned before. Please find below an example of a repository using plain SQL queries
and `JdbcTemplate`:

```java
@AllArgsConstructor
class BookDatabaseRepository implements BookRepository, FindAvailableBook, FindBookOnHold {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Option<Book> findBy(BookId bookId) {
        return findBookById(bookId)
                .map(BookDatabaseEntity::toDomainModel);
    }

    private Option<BookDatabaseEntity> findBookById(BookId bookId) {
        return Try
                .ofSupplier(() -> of(jdbcTemplate.queryForObject("SELECT b.* FROM book_database_entity b WHERE b.book_id = ?",
                                     new BeanPropertyRowMapper<>(BookDatabaseEntity.class), bookId.getBookId())))
                .getOrElse(none());
    }
    
    ...
}
```  

### Architecture-code gap

### Model-code gap
In our project we do our best to reduce _model-code gap_ to bare minimum. It means we try to put equal attention
to both the model and the code and keep them consistent. Below you will find some examples.

#### Placing on hold
![Placing on hold](docs/images/placing_on_hold.jpg)

Starting with the easiest part, below you will find the model classes corresponding to depicted command and events:

```java
@Value
class PlaceOnHoldCommand {
    ...
}
```
```java
@Value
class BookPlacedOnHold implements PatronBooksEvent {
    ...
}
```
```java
@Value
class MaximumNumberOhHoldsReached implements PatronBooksEvent {
    ...    
}
```
```java
@Value
class BookHoldFailed implements PatronBooksEvent {
    ...
}
```

We know it might not look impressive now, but if you have a look at the implementation of an aggregate,
you will see that the code reflects not only the aggregate name, but also the whole scenario of `PlaceOnHold` 
command handling. Let us uncover the details:

```java
public class PatronBooks {

    public Either<BookHoldFailed, BookPlacedOnHoldEvents> placeOnHold(AvailableBook book) {
        return placeOnHold(book, HoldDuration.openEnded());
    }
    
    ...
}    
```

The signature of `placeOnHold` method screams, that it is possible to place a book on hold only when it
is available (more information about protecting invariants by compiler you will find in [Type system section](#type-system)).
Moreover, if you try to place available book on hold it can **either** fail (`BookHoldFailed`) or produce some events -
what events?

```java
@Value
class BookPlacedOnHoldEvents implements PatronBooksEvent {
    @NonNull UUID eventId = UUID.randomUUID();
    @NonNull UUID patronId;
    @NonNull BookPlacedOnHold bookPlacedOnHold;
    @NonNull Option<MaximumNumberOhHoldsReached> maximumNumberOhHoldsReached;

    @Override
    public Instant getWhen() {
        return bookPlacedOnHold.when;
    }

    public static BookPlacedOnHoldEvents events(BookPlacedOnHold bookPlacedOnHold) {
        return new BookPlacedOnHoldEvents(bookPlacedOnHold.getPatronId(), bookPlacedOnHold, Option.none());
    }

    public static BookPlacedOnHoldEvents events(BookPlacedOnHold bookPlacedOnHold, MaximumNumberOhHoldsReached maximumNumberOhHoldsReached) {
        return new BookPlacedOnHoldEvents(bookPlacedOnHold.patronId, bookPlacedOnHold, Option.of(maximumNumberOhHoldsReached));
    }

    public List<DomainEvent> normalize() {
        return List.<DomainEvent>of(bookPlacedOnHold).appendAll(maximumNumberOhHoldsReached.toList());
    }
}
```

`BookPlacedOnHoldEvents` is a container for `BookPlacedOnHold` event, and - if patron has 5 book placed on hold already -
`MaximumNumberOfHoldsReached` (please mind the `Option` monad). You can see now how perfectly the code reflects
the model.

It is not everything, though. In the picture above you can also see a big rectangular yellow card with rules (policies)
that define the conditions that need to be fulfilled in order to get the given result. All those rules are implemented 
as functions **either** allowing or rejecting the hold:

![Restricted book policy](docs/images/placing-on-hold-policy-restricted.png)
```java
PlacingOnHoldPolicy onlyResearcherPatronsCanHoldRestrictedBooksPolicy = (AvailableBook toHold, PatronBooks patron, HoldDuration holdDuration) -> {
    if (toHold.isRestricted() && patron.isRegular()) {
        return left(Rejection.withReason("Regular patrons cannot hold restricted books"));
    }
    return right(new Allowance());
};
```

![Overdue checkouts policy](docs/images/placing-on-hold-policy-overdue.png)

```java
PlacingOnHoldPolicy overdueCheckoutsRejectionPolicy = (AvailableBook toHold, PatronBooks patron, HoldDuration holdDuration) -> {
    if (patron.overdueCheckoutsAt(toHold.getLibraryBranch()) >= OverdueCheckouts.MAX_COUNT_OF_OVERDUE_RESOURCES) {
        return left(Rejection.withReason("cannot place on hold when there are overdue checkouts"));
    }
    return right(new Allowance());
};
```

![Max number of holds policy](docs/images/placing-on-hold-policy-max.png)

```java
PlacingOnHoldPolicy regularPatronMaximumNumberOfHoldsPolicy = (AvailableBook toHold, PatronBooks patron, HoldDuration holdDuration) -> {
    if (patron.isRegular() && patron.numberOfHolds() >= PatronHolds.MAX_NUMBER_OF_HOLDS) {
        return left(Rejection.withReason("patron cannot hold more books"));
    }
    return right(new Allowance());
};
```

![Open ended hold policy](docs/images/placing-on-hold-policy-open-ended.png)

```java
PlacingOnHoldPolicy onlyResearcherPatronsCanPlaceOpenEndedHolds = (AvailableBook toHold, PatronBooks patron, HoldDuration holdDuration) -> {
    if (patron.isRegular() && holdDuration.isOpenEnded()) {
        return left(Rejection.withReason("regular patron cannot place open ended holds"));
    }
    return right(new Allowance());
};
```

### HATEOAS

### Tests
Tests are written in a BDD manner, expressing stories defined with Example Mapping.
It means we utilize both TDD and Domain Language discovered with Event Storming. 

We also made an effort to show how to create a DSL, that enables to write
tests as if they were sentences taken from the domain descriptions. Please
find an example below:

```groovy
def 'should make book available when hold canceled'() {
    given:
        BookDSL bookOnHold = aCirculatingBook() with anyBookId() locatedIn anyBranch() placedOnHoldBy anyPatron()
    and:
        PatronBooksEvent.BookHoldCanceled bookHoldCanceledEvent = the bookOnHold isCancelledBy anyPatron()

    when:
        AvailableBook availableBook = the bookOnHold reactsTo bookHoldCanceledEvent
    then:
        availableBook.bookId == bookOnHold.bookId
        availableBook.libraryBranch == bookOnHold.libraryBranchId
        availableBook.version == bookOnHold.version
}
``` 
_Please also note the **when** block, where we manifest the fact that books react to 
cancellation event_

## How to contribute

The project is still under construction, so if you like it enough to collaborate, just let us
know or simply create a Pull Request.