package org.eclipse.jface.databinding.conformance.util;

import java.util.List;

import org.eclipse.core.databinding.observable.map.IMapChangeListener;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.MapChangeEvent;

/**
 * Listener for tracking the firing of ChangeEvents.
 */
public class MapChangeEventTracker implements IMapChangeListener {
	public int count;

	public MapChangeEvent event;

	public List queue;
	
	public MapChangeEventTracker() {
		this(null);
	}
	
	public MapChangeEventTracker(List queue) {
		this.queue = queue;
	}

	public void handleMapChange(MapChangeEvent event) {
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
	public static MapChangeEventTracker observe(IObservableMap observable) {
		MapChangeEventTracker tracker = new MapChangeEventTracker();
		observable.addMapChangeListener(tracker);
		return tracker;
	}
}
