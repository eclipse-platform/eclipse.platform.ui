

E4/Event Processing
===================

< [E4](/E4 "E4")

Contents
--------

*   [1 Event Notifications in E4](#Event-Notifications-in-E4)
*   [2 What is the Problem with Events?](#What-is-the-Problem-with-Events.3F)
*   [3 Is there a way to solve the problem?](#Is-there-a-way-to-solve-the-problem.3F)
*   [4 Eclipse-specific design parameters](#Eclipse-specific-design-parameters)
*   [5 EventAdmin: pros and cons](#EventAdmin:-pros-and-cons)
*   [6 Conclusion](#Conclusion)
*   [7 References](#References)

Event Notifications in E4
-------------------------

Please refer to the IEventBroker API described on [bug 288999](https://bugs.eclipse.org/bugs/show_bug.cgi?id=288999). **This is now part of [Eclipse4](/Eclipse4 "Eclipse4").**

What is the Problem with Events?
--------------------------------

The current event processing in Eclipse is centered on the listener mechanism (also commonly described as the ["Observer"](http://en.wikipedia.org/wiki/Observer_pattern) pattern). This is a good, relatively simple, and easily customizable approach. However, two drawbacks became apparent as its usage grew: _forced lifecycle_ and _multiple implementations_.

[![Observer pattern](/images/3/35/Events_listener.png)](/File:Events_listener.png "Observer pattern")

I use "_forced lifecycle_" term to describe a situation in which lifecycle of the listener is tied to the lifecycle of the event provider. The listener can't subscribe to the event before event provider is instantiated. On the other end, the listener has to be decommissioned when the event provider is disposed. This seemingly small detail of tying the lifecycles translates into a lot of extra code and, depending on the application, might become a major bug source.

The "_multiple implementations_" problem is another aspect that grows with the software size. Even as parts of the pattern are provided by the Platform (ListenerList, SafeRunnable, Job), there is still code that needs to be added by every event generator to tie them together. From a memory consumption angle, every event provider has to instantiate and maintain its own listener list, even if, as often happens, nobody is listening or the particular event does not happen. A quick search shows over 200 places in the SDK code alone that create ListenerList's. Chances are, there are as many listener mechanisms that have their own implementations or use ListenerList indirectly.

Is there a way to solve the problem?
------------------------------------

Yes. There is a different event processing pattern commonly referred to as the ["publish/subscribe"](http://en.wikipedia.org/wiki/Publish/subscribe) approach.

[![Publish/subscribe pattern](/images/d/dc/Events_subscribe.png)](/File:Events_subscribe.png "Publish/subscribe pattern")

The main difference is that an intermediary is introduced between the sender and the receiver: events are published to the Event Broker which dispatches them to the listeners subscribed to this event type.

In this approach listeners can subscribe and unsubscribe as they please, regardless if the particular event source exists. It means that there is one implementation that everybody can use without the need to write additional code. It also means that no extra processing will be done for events that do not happen. And we'll have no need for multitude of listener interfaces specific to each event.

So, is this the best thing since sliced bread? Well, it does have some downsides. The event broker becomes a rather sensitive point of the system. It has to perform well both in CPU timing and memory allocations. And the broker itself better be robust.

Eclipse-specific design parameters
----------------------------------

Not surprisingly, there are lots of different implementations of the "publish/subscribe" pattern. They range from more basic, like the OSGi's EventAdmin, to full blown enterprise scale implementations with support for event persistence and redundancy.

What kind of implementation we need for Eclipse SDK?

*   Simple to develop (we don't have a team to work on this for the next two years)
*   Trivial to use (user's guide should fit on half a page)
*   It should support both synchronous and asynchronous processing of events
*   It should have a reasonable fallback for "event storms"
*   The expected peak load will be 1,000 events per second with 1,000 event types and 10,000 listeners
*    ? Minimum supported JRE?

Nice to have:

*   Optimized for events with a single Object as event payload with a possibility to pass multiple Object's

Out of scope, unless somebody provides patches:

*   Event persistence
*   Cross-instance data marshalling
*   Emulating memory state in the past

EventAdmin: pros and cons
-------------------------

When looking at existing implementations, OSGi's Event Admin jumps as the implementation to adopt. It is relatively simple, well described by the OSGi spec, with the reference implementation already in the Eclipse CVS repository. Moreover, Equinox team intends to include it in the Eclipse SDK 3.6.

There are some wrinkles around the implementation, but most of them are of the normal "solvable" types. One big reservation comes from the fact that it has to conform to the OSGi spec.

Consider this specific problem in conforming to the spec: in the Event Admin, every single event needs to have a Map added to it. And it can't be null or an empty Map either, as the spec says that it includes the event's topic name.

After some experimenting I found that actual memory churn added by the EventAdmin is not too bad. In absolute numbers, its dispatch times were in the range of about 10 micro seconds per event for the expected load. Garbage collection was fine on long runs, probably adding an extra minor GC every 15 seconds, with the total GC time not affected in a measurable way.

The extra memory allocated by Event's data was about 60 bytes per event. Interestingly, depending on which profiler data we were to believe, the amount of memory allocated per Event in the EventAdmin processing was somewhere between 100 bytes (good) and 4Kbytes (hmm...). I was not able to track this down as the profiler I used consistently crashed when asked to explore this spot, probably due to the size of data being in the hundreds of thousands.

(Performance numbers were measured on "nothing special" WinXP system with 2Ghz CPU and 4Gb of RAM.)

It seems that, for the expected load, extra memory allocation by the EventAdmin won't create measurable slowdowns. That said, I am still uncomfortable with the need to create Map's when most events will only be passing a single Object.

Conclusion
----------

There is no question that Eclipse outgrew the listener mechanism and is ready for the "publish/subscribe" approach. Even more interesting possibilities will open up if we consider its combination with contexts and data injection.

As for the implementation, we have two feasible choices: build based on the existing Equinox OSGi's EventAdmin implementation, or start from scratch. My choice would be to try to use EventAdmin adding parallel APIs in the Equinox namespace and changing its implementation to be optimized for the Eclipse SDK use while continuing to support OSGi's spec.

References
----------

Faison, T. Event-Based Programming: Taking Events to the Limit. APress Academic, 2006.

[Kriens, P., Hargrave, BJ. Listeners Considered Harmful: The “Whiteboard” Pattern.](http://www.osgi.org/wiki/uploads/Links/whiteboard.pdf)

[Message-oriented middleware](http://en.wikipedia.org/wiki/Message-oriented_middleware)

[Abu-Eid, V. Event Admin Service specification explained by Example.](http://www.dynamicjava.org/articles/osgi-compendium/event-admin-service)

[Advanced Message Queuing Protocol Specification](http://www.iona.com/opensource/amqp/)

[Java Message Service](http://java.sun.com/products/jms/)

[Introducing the Message Queue Interface](http://publib.boulder.ibm.com/infocenter/wmqv6/v6r0/index.jsp?topic=/com.ibm.mq.csqzal.doc/fg11380_.htm)

[Dojo's Publish and Subscribe Events](http://www.dojotoolkit.org/book/dojo-book-0-9/part-3-programmatic-dijit-and-dojo/event-system/publish-and-subscribe-events)

[Howes, T. Network Working Group RFC 1960. A String Representation of LDAP Search Filters.](http://www.ietf.org/rfc/rfc1960.txt)

Eugster, P., Felber, P., Guerraoui, R., Kermarrec, A. The Many Faces of Publish/Subscribe. ACM Computing Surveys, Vol. 35, No. 2, June 2003, pp. 114–131.

Retrieved from "[https://wiki.eclipse.org/index.php?title=E4/Event_Processing&oldid=365491](https://wiki.eclipse.org/index.php?title=E4/Event_Processing&oldid=365491)"
