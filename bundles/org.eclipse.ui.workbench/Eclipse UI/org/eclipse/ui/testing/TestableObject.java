/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.testing;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.SafeRunnable;

/**
 * A testable object.
 * Allows a test harness to register for test-related lifecycle events,
 * such as when is an appropriate time to run tests on the object.
 * Also provides API for running tests as a runnable, and for signaling
 * when the tests are starting and when they are finished.
 * <p>
 * An application will typically provide its own subclass of this class,
 * which will fire testable events at the appropriate times, and will
 * reimplement the <code>run</code> method in a way that is appropriate
 * to the application.
 * </p>
 * 
 * @since 3.0
 */
public class TestableObject {

	private ListenerList listenerList = new ListenerList();
	
	/**
	 * Adds a listener to this testable object. 
	 * Has no effect if an identical listener is already registered.
	 * <p>
	 * Testable listeners are informed about test-related lifecycle
	 * of the testable object.  The main event is when the tests can be run. 
	 * </p>
	 *
	 * @param listener a testable listener
	 */
	public void addTestableListener(ITestableListener listener) {
		listenerList.add(listener);
	}
	
	/**
	 * Removes a listener from this testable object. 
	 * Has no effect if an identical listener is already registered.
	 *
	 * @param listener a testable listener
	 */
	public void removeTestableListener(ITestableListener listener) {
		listenerList.remove(listener);
	}
	
	/**
	 * Notifies all registered listeners of the given event.
	 * 
	 * @param event the event object
	 */
	protected void fireTestableEvent(final TestableEvent event) {
		Object[] listeners = listenerList.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final ITestableListener listener = (ITestableListener) listeners[i];
			Platform.run(
				new SafeRunnable() {
					public void run() throws Exception {
						listener.testableEvent(event);
					}
				});
		}
	}
	
	/**
	 * Runs the given test runnable.
	 * The default implementation simply invokes <code>run</code> on the
	 * given test runnable.  Subclasses may extend.
	 * 
	 * @param testRunnable the test runnable to run
	 */
	public void runTest(Runnable testRunnable) {
		testRunnable.run();
	}

	/**
	 * Notification from the test harness that it is starting to run
	 * the tests.
	 * The default implementation does nothing.
	 * Subclasses may override.
	 */
	public void testingStarting() {
	}

	/**
	 * Notification from the test harness that it has finished running the
	 * tests.
	 * The default implementation does nothing.
	 * Subclasses may override.
	 */
	public void testingFinished() {
	}
}
