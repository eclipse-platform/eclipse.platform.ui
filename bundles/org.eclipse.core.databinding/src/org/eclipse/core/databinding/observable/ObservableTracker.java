/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 210115, 146397
 *******************************************************************************/
package org.eclipse.core.databinding.observable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.internal.databinding.IdentityWrapper;
import org.eclipse.core.runtime.Assert;

/**
 * This class makes it possible to monitor whenever an IObservable is read from.
 * This can be used to automatically attach and remove listeners. How to use it:
 * 
 * <p>
 * If you are implementing an IObservable, invoke getterCalled(this) whenever a
 * getter is called - that is, whenever your observable is read from. You only
 * need to do this once per method call. If one getter delegates to another, the
 * outer getter doesn't need to call the method since the inner one will.
 * </p>
 * 
 * <p>
 * If you want to determine what observables were used in a particular block of
 * code, call runAndMonitor(Runnable). This will execute the given runnable and
 * return the set of observables that were read from.
 * </p>
 * 
 * <p>
 * This can be used to automatically attach listeners. For example, imagine you
 * have a block of code that updates some widget by reading from a bunch of
 * observables. Whenever one of those observables changes, you want to re-run
 * the code and cause the widget to be refreshed. You could do this in the
 * traditional manner by attaching one listener to each observable and
 * re-running your widget update code whenever one of them changes, but this
 * code is repetitive and requires updating the listener code whenever you
 * refactor the widget updating code.
 * </p>
 * 
 * <p>
 * Alternatively, you could use a utility class that runs the code in a
 * runAndMonitor block and automatically attach listeners to any observable used
 * in updating the widget. The advantage of the latter approach is that it,
 * eliminates the code for attaching and detaching listeners and will always
 * stay in synch with changes to the widget update logic.
 * </p>
 * 
 * @since 1.0
 */
public class ObservableTracker {

	/**
	 * Threadlocal storage pointing to the current Set of IObservables, or null
	 * if none. Note that this is actually the top of a stack. Whenever a method
	 * changes the current value, it remembers the old value as a local variable
	 * and restores the old value when the method exits.
	 */
	private static ThreadLocal currentChangeListener = new ThreadLocal();

	private static ThreadLocal currentStaleListener = new ThreadLocal();

	private static ThreadLocal currentObservableSet = new ThreadLocal();

	/**
	 * Invokes the given runnable, and returns the set of IObservables that were
	 * read by the runnable. If the runnable calls this method recursively, the
	 * result will not contain IObservables that were used within the inner
	 * runnable.
	 * 
	 * @param runnable
	 *            runnable to execute
	 * @param changeListener
	 *            listener to register with all accessed observables
	 * @param staleListener
	 *            listener to register with all accessed observables, or
	 *            <code>null</code> if no stale listener is to be registered
	 * @return an array of unique observable objects
	 */
	public static IObservable[] runAndMonitor(Runnable runnable,
			IChangeListener changeListener, IStaleListener staleListener) {
		// Remember the previous value in the listener stack
		Set lastObservableSet = (Set) currentObservableSet.get();
		IChangeListener lastChangeListener = (IChangeListener) currentChangeListener
				.get();
		IStaleListener lastStaleListener = (IStaleListener) currentStaleListener
				.get();

		Set observableSet = new HashSet();
		// Push the new listeners to the top of the stack
		currentObservableSet.set(observableSet);
		currentChangeListener.set(changeListener);
		currentStaleListener.set(staleListener);
		try {
			runnable.run();
		} finally {
			// Pop the new listener off the top of the stack (by restoring the
			// previous listener)
			currentObservableSet.set(lastObservableSet);
			currentChangeListener.set(lastChangeListener);
			currentStaleListener.set(lastStaleListener);
		}

		int i = 0;
		IObservable[] result = new IObservable[observableSet.size()];
		for (Iterator it = observableSet.iterator(); it.hasNext();) {
			IdentityWrapper wrapper = (IdentityWrapper) it.next();
			result[i++] = (IObservable) wrapper.unwrap();
		}

		return result;
	}
	
	/**
	 * Runs the given runnable without tracking dependencies.
	 * @param runnable
	 * 
	 * @since 1.1
	 */
	public static void runAndIgnore(Runnable runnable) {
		// Remember the previous value in the listener stack
		Set lastObservableSet = (Set) currentObservableSet.get();
		IChangeListener lastChangeListener = (IChangeListener) currentChangeListener
				.get();
		IStaleListener lastStaleListener = (IStaleListener) currentStaleListener
				.get();
		currentObservableSet.set(null);
		currentChangeListener.set(null);
		currentStaleListener.set(null);
		try {
			runnable.run();
		} finally {
			// Pop the new listener off the top of the stack (by restoring the
			// previous listener)
			currentObservableSet.set(lastObservableSet);
			currentChangeListener.set(lastChangeListener);
			currentStaleListener.set(lastStaleListener);
		}
	}

	/*
	 * Returns the same string as the default Object.toString() implementation.
	 * getterCalled() uses this method IObservable.toString() to avoid infinite
	 * recursion and stack overflow.
	 */
	private static String toString(IObservable observable) {
		return observable.getClass().getName() + "@" //$NON-NLS-1$
				+ Integer.toHexString(System.identityHashCode(observable));
	}

	/**
	 * Notifies the ObservableTracker that an observable was read from. The
	 * JavaDoc for methods that invoke this method should include the following
	 * tag: "@TrackedGetter This method will notify ObservableTracker that the
	 * receiver has been read from". This lets callers know that they can rely
	 * on automatic updates from the object without explicitly attaching a
	 * listener.
	 * 
	 * @param observable
	 */
	public static void getterCalled(IObservable observable) {
		if (observable.isDisposed())
			Assert.isTrue(false, "Getter called on disposed observable " //$NON-NLS-1$
					+ toString(observable));
		Realm realm = observable.getRealm();
		if (!realm.isCurrent())
			Assert.isTrue(false, "Getter called outside realm of observable " //$NON-NLS-1$
					+ toString(observable));

		Set lastObservableSet = (Set) currentObservableSet.get();
		if (lastObservableSet == null) {
			return;
		}
		IChangeListener lastChangeListener = (IChangeListener) currentChangeListener
				.get();
		IStaleListener lastStaleListener = (IStaleListener) currentStaleListener
				.get();

		boolean added = false;
		if (lastObservableSet != null) {
			added = lastObservableSet.add(new IdentityWrapper(observable));
		}

		// If anyone is listening for observable usage...
		if (added && lastChangeListener != null) {
			observable.addChangeListener(lastChangeListener);
		}
		if (added && lastStaleListener != null) {
			observable.addStaleListener(lastStaleListener);
		}
	}
}
