# Design Level EventStorming

As soon as we got our examples written down, we could start digging deep into each of them, identifying key interactions
with the system, spotting the business rules and constantly refining the model. In the following sections you will
find mentioned examples modelled with Design Level EventStorming,

## Holding
### Close-ended book holding

The first example is _the one when regular patron tries to place his 6th hold_:
   
![Holding example 1](images/dl/holding/example-1.png)  

What you can see here is that we are assuming, that a particular patron has already placed 5 books on hold.
Next, in order to place one more, a patron needs to interact with _the system_ somehow, so this is the reason
for placing a blue sticky note representing a command called **place on hold**. In order to make such decision,
a patron needs to have some view of the book that can be potentially placed on hold (green sticky note).
Because the regular patron cannot place more than 5 books on hold, we could identify a rule (rectangular yellow sticky note),
that describes conditions that needs to be fulfilled for the **Book hold failed** event to occur.

Fair enough, let's go further.

### Open-ended book holding  

![Holding example 2](images/dl/holding/example-2.png)  
![Holding example 3](images/dl/holding/example-3.png)  
![Holding example 4](images/dl/holding/example-4.png)  
![Holding example 5](images/dl/holding/example-5.png)  
![Holding example 6](images/dl/holding/example-6.png)  
![Holding example 7](images/dl/holding/example-7.png)  
![Holding example 8](images/dl/holding/example-8.png)  
![Holding example 9](images/dl/holding/example-9.png)  
![Holding example 10](images/dl/holding/example-10.png)  
![Holding example 11](images/dl/holding/example-11.png)  
![Holding example 12](images/dl/holding/example-12.png)  
![Holding example 13](images/dl/holding/example-13.png)    

## Canceling a hold

![Canceling hold example 1](images/dl/cancelinghold/example-1.png)  
![Canceling hold example 2](images/dl/cancelinghold/example-2.png)  
![Canceling hold example 3](images/dl/cancelinghold/example-3.png)  
![Canceling hold example 4](images/dl/cancelinghold/example-4.png)  
![Canceling hold example 5](images/dl/cancelinghold/example-5.png)  

## Checkout

![Checkout example 1](images/dl/bookcheckouts/example-1.png)  
![Checkout example 2](images/dl/bookcheckouts/example-2.png)  
![Checkout example 3](images/dl/bookcheckouts/example-3.png)  
![Checkout example 4](images/dl/bookcheckouts/example-4.png)  
![Checkout example 5](images/dl/bookcheckouts/example-5.png)  

## Expiring a hold

![Expiring hold example 1](images/dl/expiringhold/example-1.png)  
![Expiring hold example 2](images/dl/expiringhold/example-2.png)  
![Expiring hold example 3](images/dl/expiringhold/example-3.png)  

## Registering overdue checkout

![Overdue checkout example 1](images/dl/overduecheckouts/example-1.png)  
![Overdue checkout example 2](images/dl/overduecheckouts/example-2.png)  

## Adding to catalogue

![Catalogue example 1](images/dl/addingtocatalogue/example-1.png)  
![Catalogue example 2](images/dl/addingtocatalogue/example-2.png)  