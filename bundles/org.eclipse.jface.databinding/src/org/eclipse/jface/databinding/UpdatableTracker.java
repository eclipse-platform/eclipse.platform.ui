package org.eclipse.jface.databinding;

import java.util.HashSet;
import java.util.Set;

/**
 * This class makes it possible to monitor whenever an IReadable is read from.
 * This can be used to automatically attach and remove listeners. How to use it:
 * 
 * <p>
 * If you are implementing an IReadable, invoke getterCalled(this) whenever
 * a getter is called - that is, whenever your updatable is read from. You 
 * only need to do this once per method call. If one getter delegates to another,
 * the outer getter doesn't need to call the method since the inner one will.
 * </p>
 * 
 * <p>
 * If you want to determine what updatables were used in a particular block of
 * code, call runAndMonitor(Runnable, IUpdatableListener). This will execute
 * the given runnable and notify the IUpdatableListener whenever the runnable reads from
 * an updatable.
 * </p>
 * 
 * <p>
 * This can be used to automatically attach listeners. For example, imagine
 * you have a block of code that updates some widget by reading from a bunch of updatables. 
 * Whenever one of those updatables changes, you want to re-run the code
 * and cause the widget to be refreshed. You could do this in the traditional manner by attaching
 * one listener to each updatable and re-running your widget update code whenever one of them changes, but
 * this code is repetitive and requires updating the listener code whenever you refactor the widget updating
 * code.
 * </p>
 * 
 * <p>
 * Alternatively, you could use a utility class that runs the code in a runAndMonitor
 * block and automatically attach listeners to any updatable used in updating the widget. The advantage
 * of the latter approach is that it, eliminates the code for attaching and detaching listeners and will
 * always stay in synch with changes to the widget update logic.
 * </p>
 * 
 * @since 3.2
 */
public class UpdatableTracker {
	
	/**
	 * Threadlocal storage pointing to the current Set of IUpdatables, or null if none.
	 * Note that this is actually the top of a stack. Whenever a method changes the
	 * current value, it remembers the old value as a local variable and restores the
	 * old value when the method exits.
	 */
    private static ThreadLocal currentListener = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return null;
        }
    };
    
	/**
	 * Invokes the given runnable, and returns the set of IUpdatables that were read
	 * by the runnable. If the runnable calls this method recursively, the result
	 * will not contain IUpdatables that were used within the inner runnable.
	 * 
	 * @param runnable runnable to execute
	 * @param listener listener to notify about updatables used in the runnable
	 */
	public static Set runAndMonitor(Runnable runnable) {
		// Remember the previous value in the listener stack
		Set lastValue = (Set)currentListener.get();
		
		Set nextValue = new HashSet();
		
		// Push the new listener to the top of the stack
		currentListener.set(nextValue);
		try {
			runnable.run();
		} finally {
			// Pop the new listener off the top of the stack (by restoring the previous listener)
			currentListener.set(lastValue);
		}
		
		return nextValue;
	}
	
	/**
	 * Notifies any IUpdatableListeners that an updatable was read from. The JavaDoc for 
	 * methods that invoke this method should include the following tag: 
	 * "@TrackedGetter This method will notify UpdateTracker that the reciever has been read from".
	 * This lets callers know that they can rely on automatic updates from the object without explicitly
	 * attaching a listener.
	 */
	public static void getterCalled(IReadable changed) {
		Set lastValue = (Set)currentListener.get();

		// If anyone is listening for updatable usage...
		if (lastValue != null) {
			// Remember that the current runnable depends on this updatable
			lastValue.add(changed);
		}
	}
}
