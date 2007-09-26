package org.eclipse.jface.databinding.conformance.util;

import java.util.List;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;

/**
 * Listener for tracking the firing of ChangeEvents.
 */
public class ChangeEventTracker implements IChangeListener {
	public int count;
	public ChangeEvent event;

	/**
	 * Queue that the listener will add itself too when it is notified of an
	 * event. Used to determine order of notifications of listeners.  Can be null.
	 */
	public final List queue;

	public ChangeEventTracker() {
		queue = null;
	}

	public ChangeEventTracker(List notificationQueue) {
		this.queue = notificationQueue;
	}

	public void handleChange(ChangeEvent event) {
		count++;
		this.event = event;
		if (queue != null) {
			queue.add(this);
		}
	}

	/**
	 * Convenience method to register a new listener.
	 * 
	 * @param observable
	 * @return tracker
	 */
	public static ChangeEventTracker observe(IObservable observable) {
		ChangeEventTracker tracker = new ChangeEventTracker();
		observable.addChangeListener(tracker);
		return tracker;
	}
}
