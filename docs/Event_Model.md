
Eclipse4/RCP/Event Model
========================

Contents
--------

*   [1 Introduction](#Introduction)
*   [2 Getting an IEventBroker](#Getting-an-IEventBroker)
*   [3 Posting an Event](#Posting-an-Event)
*   [4 Responding to Events](#Responding-to-Events)
    *   [4.1 Dependency Injection](#Dependency-Injection)
    *   [4.2 Subscribing Using IEventBroker](#Subscribing-Using-IEventBroker)
*   [5 UI Model Events](#UI-Model-Events)
    *   [5.1 UIEvents Structure](#UIEvents-Structure)

Introduction
============

Eclipse 4 uses a single global event bus with a publish and subscribe event model. 
The rationale for using this strategy is described in [Event Processing in E4](/E4/Event_Processing "E4/Event Processing"). 
The global event bus is implemented on top of the OSGi eventing engine and is accessed using org.eclipse.e4.core.services.events.IEventBroker.

Getting an IEventBroker
=======================

IEventBroker is model after the [OSGi EventAdmin](http://www.osgi.org/javadoc/r4v41/org/osgi/service/event/EventAdmin.html) service and provides methods for subscribing and unsubscribing to events on the bus, as well as methods for posting events to the bus.

An instance of the IEventBroker is typically available in the EclipseContext

    …
    private IEclipseContext eclipseContext;
    …
    IEventBroker eventBroker = eclipseContext.get(IEventBroker.class);

or is provided via dependency injection.

    @Inject
    IEventBroker eventBroker;

Posting an Event
================

Publishing an event on the global event bus is as simple as calling one of two methods

    IEventBroker.post(String topic, Object data) // asynchronous delivery

or

    IEventBroker.send(String topic, Object data) // synchronous delivery

Example:

    ...
    foo.Bar payload = getPayload();
    boolean wasDispatchedSuccessfully = eventBroker.send(TOPIC_STRING, payload);

If the payload of the send or post command is a regular Java object, the payload is attached to the OSGi event as a property with the key IEventBroker.DATA. If the payload is a Dictionary or a Map, all the values from the collection are added as properties with their associated keys.

Responding to Events
====================

There are two methods for declaring an interest in and responding to events. Dependency injection and subscribing through IEventBroker.

Dependency Injection
--------------------

Whenever possible you should use dependency injection to register and respond to events. This technique results in less code, is easier to read and maintain and has fewer anonymous inner classes. Internally, the E4 code base does not currently use this technique for subscribing to its own events. However, this is the result of a limitation that was late in the M4 cycle. Early in the M5 cycle, we will be changing our implementations to use the dependency injection technique for UI event handling.

    @Inject @Optional
    void closeHandler(@UIEventTopic(''TOPIC_STRING'') foo.Bar payload) {
        // Useful work that has access to payload.  The instance of foo.Bar that the event poster placed on the global event bus with the topic ''TOPIC_STRING''
    }

A quick note on the visibility of the injected handler methods.

A best practice would be to mark your handler methods as private. This makes it clear to users that the method should not be called directly. The dependency injection mechanism can inject into private fields and methods so this does not cause a problem.

However, there are currently two outstanding Eclipse defects ([365455](https://bugs.eclipse.org/bugs/show_bug.cgi?id=365455) and [365437](https://bugs.eclipse.org/bugs/show_bug.cgi?id=365437)) that can pose problems when injecting into private methods. You can declare your handler methods as package private, as in the example, to avoid these problems. If you really want to declare your methods private, please take a look at [problems injecting into private methods](/Eclipse4/DI/Problems "Eclipse4/DI/Problems") to see what you should be aware of until the defects are fixed.

Subscribing Using IEventBroker
------------------------------

In some circumstances you will not be able to use dependency injection and must subscribe to events directly. This is the case if the class you are editing does not use dependency injection. You can usually tell this because the class does not already contain any @Inject annotations.

You also can not use dependency injection if the topic string you are registering is constructed at run time. The annotation strings need to be available and complete at compile time to be used by @UIEventTopic() (This is the reason we have not previously used dependency injection internally to subscribe to UIEvents ... but that will be changing in M5)



    IEventBroker eventBroker;
    …
    void addSubscribers() {
     
    eventBroker.subscribe(TOPIC_STRING, closeHandler);
    …
    }
     
    void removeSubscribers() {
        eventBroker.unsubscribe(closeHandler);
        …
    }
    …
    private org.osgi.service.event.EventHandler closeHandler = new EventHandler() {
        public void handleEvent(Event event) {
     
        // Useful work that has access
        foo.Bar payload = (foo.Bar) event.getProperty(IEventBroker.DATA);
    }

UI Model Events
===============

The Eclipse 4 UI is a model based UI with an underlying EMF Model. Part of the UI framework, UIEventPublisher, listens to EMF events and publishes corresponding OSGi Events on the global event bus.

The constants used to subscribe to and work with the UI model events are defined in [org.eclipse.e4.ui.workbench.UIEvents](http://git.eclipse.org/c/platform/eclipse.platform.ui.git/tree/bundles/org.eclipse.e4.ui.workbench/src/org/eclipse/e4/ui/workbench/UIEvents.java). The bulk of the constants in this class are generated directly from the EMF model at development time using the utility org.eclipse.e4.ui.internal.workbench.swt.GenTopic. There are a few hand-crafted constants for non UI events such as the life cycle events.

Late in the 4.2M4 milestone the hierarchy and usage pattern of UIEvents was significantly changed. This page discusses the new mechanism. For a discussion of the old mechanism and the changes you need to make to migrate from the old style to the new style take a look at [Eclipse4/UI/Event/Migration](/Eclipse4/UI/Event/Migration "Eclipse4/UI/Event/Migration").

UIEvents Structure
------------------

Each EMF model element has a corresponding interface defined in UIEvents. Each interface has two constants defined for each attribute of the model element.

Here is the example for the UILabel model element

       public static interface UILabel {
     
           // Topics that can be subscribed to
           public static final String TOPIC_ALL = "org/eclipse/e4/ui/model/ui/UILabel/*"; //$NON-NLS-1$
           public static final String TOPIC_ICONURI = "org/eclipse/e4/ui/model/ui/UILabel/iconURI/*"; //$NON-NLS-1$
           public static final String TOPIC_LABEL = "org/eclipse/e4/ui/model/ui/UILabel/label/*"; //$NON-NLS-1$
           public static final String TOPIC_TOOLTIP = "org/eclipse/e4/ui/model/ui/UILabel/tooltip/*"; //$NON-NLS-1$
     
           // Attributes that can be tested in event handlers
           public static final String ICONURI = "iconURI"; //$NON-NLS-1$
           public static final String LABEL = "label"; //$NON-NLS-1$
           public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$
       }

The TOPIC_* constants are used to subscribe to events generated when the corresponding attribute changes. The constant can be used by either the dependency injection technique or the IEventBroker.subscribe() technique described above. The TOPIC_ALL constant is used to register for changes on all the attributes of a model element. If the dependency injection technique is used, the event payload is the event itself.

    @Inject @Optional
    private void closeHandler(@UIEventTopic(UIEvents.UILifeCycle.ACTIVATE) org.osgi.service.event.Event event) {
          ...
    }

The constants named directly for the element attribute (LABEL, TOOLTIP and ICONURI from the example above) are used in event handlers if you need to inspect the event and determine which attributes have actually changed. This works because UIEventPublisher adds the attribute name from the source EMF event to the OSGi event with the property key UIEvents.EventTags.ATTNAME. The other UIEvents.EventTags.* constants list other values that may be published in an event depending on the event type. Different event types will store different tags in the event.

