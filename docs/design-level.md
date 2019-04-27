# Design Level EventStorming

As soon as we got our examples written down, we could start digging deep into each of them, identifying key interactions
with the system, spotting business rules and constantly refining the model. In the following sections you will
find mentioned examples modelled with Design Level EventStorming.


## Holding
### Regular patron

The first example is _the one when regular patron tries to place his 6th hold_:
   
![Holding example 1](images/dl/holding/example-1.png)  

What you can see here is that we are assuming, that a particular patron has already placed 5 books on hold.
Next, in order to place one more, a patron needs to interact with _the system_ somehow, so this is the reason
for placing a blue sticky note representing a command called **place on hold**. In order to make such decision,
a patron needs to have some view of the book that can be potentially placed on hold (green sticky note).
Because the regular patron cannot place more than 5 books on hold, we could identify a rule (rectangular yellow sticky note),
that describes conditions that needs to be fulfilled for the **Book hold failed** event to occur.

Fair enough, let's go further.

When a **patron** tires to place on hold a book that is currently not available it should not be possible, thus resulting
in **book hold failed** event, as it is depicted below:

![Holding example 2](images/dl/holding/example-2.png)  

Taking a look at the domain description again, we find out that each patron can have no more than 1 **overdue checkouts**.
In such situation, every attempt to **place a book on hold** should fail:

![Holding example 3](images/dl/holding/example-3.png)  

If we are talking about **regular patrons**, what is special about them is that they are not allowed to hold a
**restricted book**:
  
![Holding example 4](images/dl/holding/example-4.png)

Second thing that is not allowed for a **regular patron** is **open-ended** hold: 
![Holding example 12](images/dl/holding/example-12.png)    

All right, enough with failures, let patrons lend some books, eventually:
  
![Holding example 5](images/dl/holding/example-5.png)  

Having in mind all previous examples, we discovered following conditions that need to be fulfilled for **patron** to
**place a book on hold**:
* Book must be available
* Book must not be **restricted**
* At the moment of placing a hold, a patron cannot have more than 4 holds
* Patron cannot have more than 1 overdue checkout

And here is the last example, partially covered before:

![Holding example 6](images/dl/holding/example-6.png)  

### Regular patron

In the previous part of this paragraph we focused on a *regular patron* only. Let's have a look at *researcher patron* now.
The domain description clearly states that **any** patron with more than 2 **overdue checkouts** will get a rejection
when trying to place book on hold. So we have it modelled:
  
![Holding example 7](images/dl/holding/example-7.png)  

There is also no exception in terms of holding a book that is **not available**:

![Holding example 8](images/dl/holding/example-8.png)  

The thing that differentiates **researcher patron** from a **regular** one is that he/she can place on hold a **restricted**
book:

![Holding example 9](images/dl/holding/example-9.png)  

Last three examples depict successful holding scenarios:

![Holding example 10](images/dl/holding/example-10.png)  
![Holding example 11](images/dl/holding/example-11.png)  
![Holding example 13](images/dl/holding/example-13.png)    

## Canceling a hold

Any patron can cancel the hold. The unbreakable condition to be fulfilled is the one that the hold exists.
If it is not the case **book hold cancelling failed** event occurs. What you can spot here is that now the **patron**,
in order to cancel a hold, he/she needs to have a view of current holds (mind the **Holds view** green sticky note).

![Canceling hold example 1](images/dl/cancelinghold/example-1.png)  

If the hold is present, then it should be possible to cancel it:

![Canceling hold example 2](images/dl/cancelinghold/example-2.png)  

We also need to take care of the scenario when a **patron** tries to **cancel a hold** that was actually
not placed by himself/herself:

![Canceling hold example 3](images/dl/cancelinghold/example-3.png)  

It shouldn't be also possible to **cancel a hold** twice:

![Canceling hold example 5](images/dl/cancelinghold/example-5.png)  

Getting back to holding-related examples, let's try to join them with hold cancellation. Each **patron** can have no more
than five holds at a particular point in time. Thus, cancelling one of them should be enough for **patron** to **place
on hold** another book:
  
![Canceling hold example 4](images/dl/cancelinghold/example-4.png)  

## Checkout

Checking out is actually the essence of library functioning. **Any patron** can checkout a hold, but it is only possible
when the **hold** exists:
 
![Checkout example 1](images/dl/bookcheckouts/example-1.png)  

It is also not allowed to checkout someone else's hold:

![Checkout example 2](images/dl/bookcheckouts/example-2.png)  

An example summing things up is depicted below:

![Checkout example 3](images/dl/bookcheckouts/example-3.png)  

A real-life scenario could be that a **patron** cancels his/her hold, and tries to check the book out:
 
![Checkout example 4](images/dl/bookcheckouts/example-4.png)  

It might also happen that a **patron** has the hold, whereas the book is missing in a library:
  
![Checkout example 5](images/dl/bookcheckouts/example-5.png)  

## Expiring a hold

According to the domain description, any **close-ended hold** is active until it is either checked out by **patron** or
expired. The expiration check is done automatically by the system at the **beginning of the day**. In order to find holds
that qualify to expiration, a system needs to have a read model of such entries. Domain description names it a **Daily sheet**
(please mind the green sticky note)

![Expiring hold example 1](images/dl/expiringhold/example-1.png)  

When the book is **placed on hold** and the hold is **cancelled** before its expiration due date, it shouldn't be registered
as expired hold:

![Expiring hold example 2](images/dl/expiringhold/example-2.png)  

The expiration check should mark each hold as expired only once:

![Expiring hold example 3](images/dl/expiringhold/example-3.png)  

## Registering overdue checkout

Each book can be checked out for not longer than 60 days. **Overdue checkouts** are identified on a daily basis by looking
at the **Daily sheet** (please mind the green sticky note):
 
![Overdue checkout example 1](images/dl/overduecheckouts/example-1.png)  

Moreover we do not expect the **returned book** to be ever registered as **overdue checkout**:

![Overdue checkout example 2](images/dl/overduecheckouts/example-2.png)  

## Adding to catalogue

![Catalogue example 1](images/dl/addingtocatalogue/example-1.png)  
![Catalogue example 2](images/dl/addingtocatalogue/example-2.png)  