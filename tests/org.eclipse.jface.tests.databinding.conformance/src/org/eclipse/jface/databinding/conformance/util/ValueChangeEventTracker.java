package org.eclipse.jface.databinding.conformance.util;

import java.util.List;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;

/**
 * Listener for tracking the firing of ValueChangeEvents.
 */
public class ValueChangeEventTracker implements IValueChangeListener {
	public int count;

	public ValueChangeEvent event;

	public final List queue;

	public ValueChangeEventTracker() {
		this(null);
	}

	public ValueChangeEventTracker(List queue) {
		this.queue = queue;
	}

	public void handleValueChange(ValueChangeEvent event) {
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
	public static ValueChangeEventTracker observe(IObservableValue observable) {
		ValueChangeEventTracker tracker = new ValueChangeEventTracker();
		observable.addValueChangeListener(tracker);
		return tracker;
	}
}
