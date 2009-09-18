package org.eclipse.e4.ui.services.events;

import org.eclipse.e4.core.services.annotations.In;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Helper base class to receive events that might need UI interaction.
 * Remember to call {@link #dispose()} method when event handler is no
 * longer needed.
 */
public abstract class UIEventHandler implements EventHandler {
	
	private IEventBroker broker;
	
	private String eventTopic;
	private String eventFilter;

	abstract public void handleUIEvent(Event event);

	/**
	 * Registers this handler for the specified topic and handler using event broker
	 * from the context, or the default event broker if none is present in the context. 
	 * @param topic
	 * @param filter
	 * @param context
	 */
	public UIEventHandler(String topic, String filter) {
		this.eventTopic = topic;
		this.eventFilter = filter;
	}
	
	/* (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	public void handleEvent(final Event event) {
		// this is very close to a no-op if run on the main thread
		Display.getDefault().syncExec(new Runnable() { 
			public void run() {
				handleUIEvent(event);
			}
		});
	}
	
	@In (optional = true)
	public void setEventBroker(IEventBroker newBroker) {
		if (broker != null)
			broker.unsubscribe(this);
		broker = newBroker;
		if (broker != null)
			broker.subscribe(eventTopic, eventFilter, this);
	}

	/**
	 * Close this handler and stop listening to events.  
	 * @return <code>true</code> if this handler was successfully unsubscribed; <code>false</code>
	 * otherwise
	 */
	public void dispose() {
		if (broker != null)
			broker.unsubscribe(this);
	}
	
}
