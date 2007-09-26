package org.eclipse.jface.databinding.conformance.util;

import java.util.List;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;

/**
 * Listener for tracking the firing of ListChangeEvents.
 */
public class ListChangeEventTracker implements IListChangeListener {
	public int count;

	public ListChangeEvent event;

	/**
	 * Queue that the listener will add itself too when it is notified of an
	 * event. Used to determine order of notifications of listeners.
	 */
	public final List listenerQueue;

	public ListChangeEventTracker() {
		this(null);
	}

	public ListChangeEventTracker(List listenerQueue) {
		this.listenerQueue = listenerQueue;
	}

	public void handleListChange(ListChangeEvent event) {
		count++;
		this.event = event;
		if (listenerQueue != null) {
			listenerQueue.add(this);
		}
	}
	
	/**
	 * Convenience method to register a new listener.
	 * 
	 * @param observable
	 * @return tracker
	 */
	public static ListChangeEventTracker observe(IObservableList observable) {
		ListChangeEventTracker tracker = new ListChangeEventTracker();
		observable.addListChangeListener(tracker);
		return tracker;
	}
}
