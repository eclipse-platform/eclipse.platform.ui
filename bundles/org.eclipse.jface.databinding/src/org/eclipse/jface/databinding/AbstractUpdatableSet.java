package org.eclipse.jface.databinding;

import java.util.Collection;

/**
 * Abstract base class for implementations of IReadableSet. Clients may subclass.
 * 
 * @since 3.2
 */
public abstract class AbstractUpdatableSet extends Updatable implements IReadableSet {
	/**
	 * Optimization: this event instance is created lazily and cached
	 * to reduce garbage collection overhead.
	 */
	private ChangeEvent cachedAddEvent = null;

	/**
	 * Optimization: this event instance is created lazily and cached
	 * to reduce garbage collection overhead.
	 */
	private ChangeEvent cachedRemoveEvent = null;

	protected final void fireAdded(Collection added) {
        if (added.size() > 0) {        	
        	ChangeEvent event;
        	
        	if (cachedAddEvent != null) {
        		event = cachedAddEvent;
        		event.newValue = added;
        		// Clear the cached event in case we need to fire something else recursively
        		cachedAddEvent = null;
        	} else {
        		event = new ChangeEvent(this, ChangeEvent.ADD_MANY, null, added);
        	}
        	
        	fireChangeEvent(event);
        	
        	cachedAddEvent = event;
        }
	}

	protected final void fireRemoved(Collection removed) {
        if (removed.size() > 0) {
        	ChangeEvent event;
        	
        	if (cachedRemoveEvent != null) {
        		event = cachedRemoveEvent;
        		event.newValue = removed;
        		// Clear the cached event in case we need to fire something else recursively
        		cachedRemoveEvent = null;
        	} else {
        		event = new ChangeEvent(this, ChangeEvent.REMOVE_MANY, null, removed);
        	}
        	
        	fireChangeEvent(event);
        	
        	cachedRemoveEvent = event;
        }
    }
	
	public final void dispose() {
	}

	protected final void fireStale(boolean isStale) {
		fireChangeEvent(createStaleEvent(isStale));
	}
		
	public final Collection toCollection() {
		UpdatableTracker.getterCalled(this);
        return computeElements();
	}

	protected abstract Collection computeElements();
}
