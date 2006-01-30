package org.eclipse.jface.databinding.updatables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.databinding.ChangeEvent;
import org.eclipse.jface.databinding.IChangeListener;
import org.eclipse.jface.databinding.ILazyReadableValue;
import org.eclipse.jface.databinding.IReadable;
import org.eclipse.jface.databinding.IUpdatable;
import org.eclipse.jface.databinding.UpdatableTracker;
import org.eclipse.jface.databinding.WritableUpdatable;

/**
 * A Lazily calculated value that automatically computes and registers 
 * listeners on its dependencies as long as all of its dependencies are 
 * IReadableValues
 * 
 * @since 3.2
 */
public abstract class LazyCalculatedValue extends WritableUpdatable implements
		ILazyReadableValue {
	
	private boolean dirty = true;
	private Object cachedValue = null;
	
	/** 
	 * Dependencies list. This is a collection that contains no duplicates. It is normally
	 * an ArrayList to conserve memory, but if it ever grows above a certain number of elements, 
	 * a HashSet is substited to conserve runtime.
	 */ 
	private Collection dependencies = new ArrayList();
	
	/**
	 * Stale count. This is equal to the number of currently-stale dependencies. The 
	 * stale count is reset to zero each time before calling computeValue.
	 */
	private int staleCount = 0;
	
	/**
	 * Inner class that implements interfaces that we don't want to expose as
	 * public API. Each interface could have been implemented using a separate anonymous 
	 * class, but we combine them here to reduce the memory overhead and number of classes.
	 * 
	 * <p>
	 * The Runnable calls computeValue and stores the result in cachedValue. 
	 * </p> 
	 * 
	 * <p>
	 * The IUpdatableListener stores each updatable in the dependencies list. This is
	 * registered as the listener when calling UpdatableTracker, to detect every 
	 * updatable that is used by computeValue.
	 * </p>
	 * 
	 * <p>
	 * The IChangeListener is attached to every dependency.
	 * </p>
	 * 
	 */	
	private class PrivateInterfaces implements Runnable, IChangeListener {		
		public void run() {
			cachedValue = calculate();
		}
		
		public void handleChange(ChangeEvent changeEvent) {
			// Called whenever something in the dependency list changes.
			
			switch(changeEvent.getChangeType()) {
			case ChangeEvent.STALE:
				boolean isStale = ((Boolean)changeEvent.getNewValue()).booleanValue();
				markStale((IReadable)changeEvent.getSource(), isStale);
			case ChangeEvent.VERIFY:
				// Ignore verify events
				return;
			default:
				// For any other type of change, mark the value as dirty
				makeDirty();
			}
		}
	}
	
	private PrivateInterfaces privateInterface = new PrivateInterfaces();
	
	/*   
	 * (non-Javadoc)
	 * @see org.eclipse.jface.databinding.ILazyReadableValue#getValue()
	 */
	public final Object getValue() {
		UpdatableTracker.getterCalled(this);

		if (dirty) {
			// Record the old stale state, so that we will know whether or not
			// to fire a stale change event.
			boolean wasStale = (staleCount != 0);
			
			// This line will do the following:
			// - Run the computeValue method
			// - While doing so, add any updatable that is touched to the dependencies list

			Set newDependencies = UpdatableTracker.runAndMonitor(privateInterface);
		    
			int newStaleCount = 0;
			for (Iterator iter = newDependencies.iterator(); iter.hasNext();) {
				IReadable next = (IReadable) iter.next();
				
				// Just count the number of stale dependencies. The stale change event will
				// be fired by getValue if necessary.
				if (next.isStale()) {
					newStaleCount++;
				}
				
				// Add a change listener to the new dependency.
				next.addChangeListener(privateInterface);				
			}
			
			staleCount = newStaleCount;
			dependencies = newDependencies;
			boolean isStale = (staleCount != 0);

			if (wasStale != isStale) {
				fireChangeEvent(createStaleEvent(isStale));
			}
			
			dirty = false;
		}

		return cachedValue;
	}
	
	/**
	 * Subclasses must override this method to provide the object's value.
	 *  
	 * @return the object's value
	 */
	protected abstract Object calculate();

	protected final void makeDirty() {
		if (!dirty) {
			dirty = true;
			
			// Stop listening for dependency changes.
			IReadable[] updatables = (IReadable[]) dependencies.toArray(new IReadable[dependencies.size()]);
			dependencies.clear();
			
			for (int i = 0; i < updatables.length; i++) {
				IReadable readable = updatables[i];
					
				readable.removeChangeListener(privateInterface);
			}
			
			// Fire the "dirty" event
			fireChangeEvent(new ChangeEvent(this, ChangeEvent.DIRTY, null, null));
		}
	}

	public void dispose() {
		// Stop listening for dependency changes
		IUpdatable[] updatables = (IUpdatable[]) dependencies.toArray(new IUpdatable[dependencies.size()]);
		dependencies.clear();
		
		for (int i = 0; i < updatables.length; i++) {
			IUpdatable updatable = updatables[i];
				
			updatable.removeChangeListener(privateInterface);
		}

		super.dispose();
	}
	
	/**
	 * Called whenever the stale state of a dependency changes. Subclasses may extend.
	 * Increments or decrements the stale count for this value. The stale count is reset
	 * to zero each time the value is recomputed. 
	 * 
	 * @param isStale true to increment, false to decrement
	 */
	private void markStale(IReadable readable, boolean isStale) {
		// Adjust the count of the number of stale dependent objects. Fire a change event
		// if necessary.
		if (isStale) {
			staleCount++;
			
			if (staleCount == 1) {
				fireChangeEvent(createStaleEvent(true));
			}
		} else {
			staleCount--;
			
			if (staleCount == 0) {
				fireChangeEvent(createStaleEvent(false));
			}
		}
	}

}
