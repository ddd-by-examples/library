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
During the session we discovered following definitions:
![Patron](https://realtimeboard.com/app/board/o9J_ky9pa54=/?moveToWidget=3074457346371442075)


### Bounded Contexts
### ArchUnit
### Functional thinking
### No ORM
### Architecture-code gap
### Model-code gap
### HATEOAS

### Tests
Tests are written in a BDD manner, expressing stories defined with Example Mapping.
It means we utilize both TDD and Domain Language discovered with Event Storming. 

We also made an effort to show how to create a DSL, that enables to write
tests as if they were sentences taken from the domain descriptions. Please
find an example below:

```
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