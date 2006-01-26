package org.eclipse.jface.databinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.util.Assert;

public class Updatable {

	/**
	 * Collection of IChangeListener. null if disposed
	 */
	private Collection changeListeners = new ArrayList();
	
	/**
	 * Cached ChangeEvent for the STALE event. This is created lazily and cached
	 * to reduce garbage collection overhead.
	 */
	private ChangeEvent staleEvent;
	
	/**
	 * Cached ChangeEvent for the NOT_STALE event. This is created lazily and
	 * cached to reduce garbage collection overhead.
	 */
	private ChangeEvent notStaleEvent;

	/**
	 * Creates a STALE change event for this updatable 
	 * 
	 * @return a ChangeEvent of type STALE with the reciever as the source
	 */
	protected final ChangeEvent createStaleEvent(boolean isStale) {
		ChangeEvent result;
		if (isStale) {
			if (staleEvent == null) {
				staleEvent = new ChangeEvent(this, ChangeEvent.STALE, null, Boolean.TRUE);
			}
			result = staleEvent;
		} else {
			if (notStaleEvent == null) {
				notStaleEvent = new ChangeEvent(this, ChangeEvent.STALE, null, Boolean.FALSE);
			}			
			result = notStaleEvent;
		}
		
		result.vetoed = false;
		
		return result;
	}
	
	public void addChangeListener(IChangeListener changeListener) {
		Assert.isNotNull(changeListener);
		if (changeListeners == null) {
			changeListeners = new ArrayList();
		}
		if (changeListeners.isEmpty()) {
			firstListenerAdded();
		}
		
		if (!changeListeners.contains(changeListener)) {
			changeListeners.add(changeListener);
		}
		
		// Optimization: if there are more than a certain number of listeners, store the 
		// listener list as a HashSet rather than a list. This prevents a bottleneck in contains()
		// if the updatable contains a large number of listeners.
		// This optimization is intended for global updatables which are widely referenced but
		// change infrequently. For example, an updatable representing the default text colour or
		// system font.
		if (changeListeners.size() > 32) {
			Collection newListeners = new HashSet();
			  
			newListeners.addAll(changeListeners);
			changeListeners = newListeners;
		}
	}
	
	/**
	 * Called when the first listener is added to this updatable. Some updatables
	 * may optimize themselves by removing listeners from other object when nobody
	 * is listening to the updatable. Such updatables may use this method to detect
	 * when they need to start tracking changes. 
	 * 
	 * <p>
	 * Important: implementations of this method must not change the current
	 * value of the updatable or fire any property changes. 
	 * </p>
	 */
	protected void firstListenerAdded() {
		
	}

	/**
	 * Called when the last listener is removed from the updatable. Some updatables
	 * may optimize themselves by removing their own listeners from other objects when
	 * nobody is listening to them. Such updatables may use this method to detect
	 * situations when they can stop tracking changes.
	 *
	 * <p>
	 * Important: implementations of this method must not change the current
	 * value of the updatable or fire any property changes. Even though the updatable
	 * is not required to listen to or fire property changes, it is still required to
	 * report the correct, most up-to-date value when asked. 
	 * </p>
	 */
	protected void lastListenerRemoved() {

	}
	
	protected final boolean hasListeners() {
		return changeListeners != null && !changeListeners.isEmpty();
	}

	public void removeChangeListener(IChangeListener changeListener) {
		Assert.isNotNull(changeListener);
		
		if (changeListeners == null) {
			return;
		}
		
		changeListeners.remove(changeListener);
		
		if (changeListeners.isEmpty()) {
			lastListenerRemoved();
		}
	}

	protected final ChangeEvent fireChangeEvent(int changeType, Object oldValue,
			Object newValue) {
		return fireChangeEvent(changeType, oldValue, newValue, ChangeEvent.POSITION_UNKNOWN);
	}
	
	protected final ChangeEvent fireChangeEvent(int changeType, Object oldValue, Object newValue,  int position) {
		return fireChangeEvent(changeType, oldValue, newValue, null, position);
	}

	protected final ChangeEvent fireChangeEvent(int changeType, Object oldValue, Object newValue, Object parent, int position) {
		
		ChangeEvent changeEvent = null;

		// Optimization: try to use a cached ChangeEvents event where possible, to reduce garbage
		// collection overhead.
		if (changeType == ChangeEvent.STALE) {
			if (oldValue == null && newValue instanceof Boolean) {
				changeEvent = createStaleEvent(((Boolean)newValue).booleanValue());
			}
		}
		
		if (changeEvent == null) {
			changeEvent = new ChangeEvent(this, changeType, oldValue,
					newValue, parent, position);
		}
		fireChangeEvent(changeEvent);
		return changeEvent;
	}

	protected final void fireChangeEvent(ChangeEvent changeEvent) {
		if (changeListeners == null) {
			return;
		}
		
		IChangeListener[] listeners = (IChangeListener[]) changeListeners
				.toArray(new IChangeListener[changeListeners.size()]);
		for (int i = 0; i < listeners.length; i++) {			
				listeners[i].handleChange(changeEvent);
		}
	}

	public boolean isStale() {
		return false;
	}

	protected void dispose() {
		if (isDisposed()) {
			return;
		}
		
		fireChangeEvent(new ChangeEvent(this, ChangeEvent.DISPOSED, null, null));
		
		changeListeners = null;
	}
	
	protected boolean isDisposed() {
		return changeListeners == null;
	}
}
