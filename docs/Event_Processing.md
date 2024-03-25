Eclipse Event Processing
========================

Contents
--------

*   [1 Event Notifications in E4](#Event-Notifications-in-Eclipse)
*   [2 What is the Problem with the observer pattern?](#what-is-the-problem-with-the-observer-pattern)
*   [3 Using publish and subscribe](#Using-publish-and-subscribe)
*   [4 Eclipse-specific design parameters](#Eclipse-specific-design-parameters)
*   [5 EventAdmin: pros and cons](#EventAdmin-pros-and-cons)
*   [6 Conclusion](#Conclusion)
*   [7 References](#References)

Event Notifications in Eclipse
------------------------------

This document was originally a working document to define the usage of a publish / subscribe mechanism in the first Eclipse 4 release.
It has been rewritten to reflect the decision taken and why.


What is the Problem with the observer pattern?
---------------------------------------------

The [observer pattern](http://en.wikipedia.org/wiki/Observer_pattern) pattern)  is a good, relatively simple, and easily customizable approach. 
However, two drawbacks became apparent as its usage grew: _forced lifecycle_ and _multiple implementations_.


![Observer pattern](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Events_listener.png)

I use "_forced lifecycle_" term to describe a situation in which lifecycle of the listener is tied to the lifecycle of the event provider. The listener can't subscribe to the event before event provider is instantiated. On the other end, the listener has to be decommissioned when the event provider is disposed. This seemingly small detail of tying the lifecycles translates into a lot of extra code and, depending on the application, might become a major bug source.

The "_multiple implementations_" problem is another aspect that grows with the software size. Even as parts of the pattern are provided by the Platform (ListenerList, SafeRunnable, Job), there is still code that needs to be added by every event generator to tie them together. From a memory consumption angle, every event provider has to instantiate and maintain its own listener list, even if, as often happens, nobody is listening or the particular event does not happen. A quick search shows over 200 places in the SDK code alone that create ListenerList's. Chances are, there are as many listener mechanisms that have their own implementations or use ListenerList indirectly.

Using publish and subscribe
---------------------------

Yes. There is a different event processing pattern commonly referred to as the [publish/subscribe](http://en.wikipedia.org/wiki/Publish/subscribe) approach.


![Publish/subscribe pattern](https://raw.githubusercontent.com/eclipse-platform/eclipse.platform.ui/master/docs/images/Events_subscribe.png)


The main difference is that an intermediary is introduced between the sender and the receiver: events are published to the Event Broker which dispatches them to the listeners subscribed to this event type.

In this approach listeners can subscribe and unsubscribe as they please, regardless if the particular event source exists. It means that there is one implementation that everybody can use without the need to write additional code. It also means that no extra processing will be done for events that do not happen. And we'll have no need for multitude of listener interfaces specific to each event.

On the downside the event broker becomes a rather sensitive point of the system. 
It has to perform well both in CPU timing and memory allocations. 
And the broker itself better be robust.

Eclipse-specific design parameters
----------------------------------

Not surprisingly, there are lots of different implementations of the "publish/subscribe" pattern. 
They range from more basic, like the OSGi's EventAdmin, to full blown enterprise scale implementations with support for event persistence and redundancy.


EventAdmin: pros and cons
-------------------------

It is relatively simple, well described by the OSGi spec, with the reference implementation already in the Eclipse repository.

According to the the Event Admin, every single event needs to have a Map added to it. 
And it can't be null or an empty Map either, as the spec says that it includes the event's topic name.

The dispatch times in EventAdmin in absolute numbers were in the range of about 10 micro seconds per event for the expected load. 
Garbage collection was fine on long runs, probably adding an extra minor GC every 15 seconds, with the total GC time not affected in a measurable way.

The extra memory allocated by Event's data was about 60 bytes per event.
Measureing this, is seemed that for the expected load, extra memory allocation by the EventAdmin won't create measurable slowdowns.
Still the creating of a Map's for every event is of concern, when most events will only be passing a single Object.

Conclusion
----------

OSGi's Event Admin was adopted. 
Eclipse publish / subscribe approach is based on the existing Equinox OSGi's EventAdmin implementation. 

References
----------

IEventBroker API described on [bug 288999](https://bugs.eclipse.org/bugs/show_bug.cgi?id=288999).

[Message-oriented middleware](http://en.wikipedia.org/wiki/Message-oriented_middleware)


