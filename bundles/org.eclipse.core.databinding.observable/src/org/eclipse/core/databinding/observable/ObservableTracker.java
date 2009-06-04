/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 210115, 146397, 249526, 262269
 *******************************************************************************/
package org.eclipse.core.databinding.observable;

import java.util.Set;

import org.eclipse.core.internal.databinding.identity.IdentitySet;
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

	private static ThreadLocal currentGetterCalledSet = new ThreadLocal();

	private static ThreadLocal currentObservableCreatedSet = new ThreadLocal();

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
		Set lastObservableSet = (Set) currentGetterCalledSet.get();
		IChangeListener lastChangeListener = (IChangeListener) currentChangeListener
				.get();
		IStaleListener lastStaleListener = (IStaleListener) currentStaleListener
				.get();

		Set observableSet = new IdentitySet();
		// Push the new listeners to the top of the stack
		currentGetterCalledSet.set(observableSet);
		currentChangeListener.set(changeListener);
		currentStaleListener.set(staleListener);
		try {
			runnable.run();
		} finally {
			// Pop the new listener off the top of the stack (by restoring the
			// previous listener)
			currentGetterCalledSet.set(lastObservableSet);
			currentChangeListener.set(lastChangeListener);
			currentStaleListener.set(lastStaleListener);
		}

		return (IObservable[]) observableSet
				.toArray(new IObservable[observableSet.size()]);
	}

	/**
	 * Invokes the given runnable, and returns the set of IObservables that were
	 * created by the runnable. If the runnable calls this method recursively,
	 * the result will not contain IObservables that were created within the
	 * inner runnable.
	 * <p>
	 * <em>NOTE: As of 1.2 (Eclipse 3.5), there are unresolved problems with this API, see
	 * <a href="https://bugs.eclipse.org/278550">bug 278550</a>. If we cannot
	 * find a way to make this API work, it will be deprecated as of 3.6.</em>
	 * </p>
	 * 
	 * @param runnable
	 *            runnable to execute
	 * @return an array of unique observable objects
	 * @since 1.2
	 */
	public static IObservable[] runAndCollect(Runnable runnable) {
		Set lastObservableCreatedSet = (Set) currentObservableCreatedSet.get();

		Set observableSet = new IdentitySet();
		// Push the new listeners to the top of the stack
		currentObservableCreatedSet.set(observableSet);
		try {
			runnable.run();
		} finally {
			// Pop the new listener off the top of the stack (by restoring the
			// previous listener)
			currentObservableCreatedSet.set(lastObservableCreatedSet);
		}

		return (IObservable[]) observableSet
				.toArray(new IObservable[observableSet.size()]);
	}

	/**
	 * Runs the given runnable without tracking dependencies.
	 * @param runnable
	 * 
	 * @since 1.1
	 */
	public static void runAndIgnore(Runnable runnable) {
		// Remember the previous value in the listener stack
		Set lastGetterCalledSet = (Set) currentGetterCalledSet.get();
		Set lastObservableCreatedSet = (Set) currentObservableCreatedSet.get();
		IChangeListener lastChangeListener = (IChangeListener) currentChangeListener
				.get();
		IStaleListener lastStaleListener = (IStaleListener) currentStaleListener
				.get();
		currentGetterCalledSet.set(null);
		currentObservableCreatedSet.set(null);
		currentChangeListener.set(null);
		currentStaleListener.set(null);
		try {
			runnable.run();
		} finally {
			// Pop the new listener off the top of the stack (by restoring the
			// previous listener)
			currentGetterCalledSet.set(lastGetterCalledSet);
			currentObservableCreatedSet.set(lastObservableCreatedSet);
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

		Set getterCalledSet = (Set) currentGetterCalledSet.get();
		if (getterCalledSet != null && getterCalledSet.add(observable)) {
			// If anyone is listening for observable usage...
			IChangeListener changeListener = (IChangeListener) currentChangeListener
					.get();
			if (changeListener != null)
				observable.addChangeListener(changeListener);
			IStaleListener staleListener = (IStaleListener) currentStaleListener
					.get();
			if (staleListener != null)
				observable.addStaleListener(staleListener);
		}
	}

	/**
	 * Notifies the ObservableTracker that an observable was created.
	 * 
	 * @param observable
	 *            the observable that was created
	 * @since 1.2
	 */
	public static void observableCreated(IObservable observable) {
		Set observableCreatedSet = (Set) currentObservableCreatedSet.get();
		if (observableCreatedSet != null) {
			observableCreatedSet.add(observable);
		}
	}
}
