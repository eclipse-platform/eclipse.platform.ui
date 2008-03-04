/*******************************************************************************
 * Copyright (c) 2007-2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 221351
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.delegate.IObservableContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;

/**
 * TestCase that provides the standard behavior expected for delegating test cases.
 * 
 * @since 3.2
 */
public class ObservableDelegateTest extends TestCase {
	private IObservableContractDelegate delegate;

	private Realm previousRealm;

	private IObservable observable;
	private String debugInfo;

	public ObservableDelegateTest(IObservableContractDelegate delegate) {
		this(null, delegate);
	}
	
	public ObservableDelegateTest(String testName, IObservableContractDelegate delegate) {
		super(testName);
		this.delegate = delegate;
	}

	protected void setUp() throws Exception {
		super.setUp();
		previousRealm = Realm.getDefault();

		delegate.setUp();
		observable = doCreateObservable();
	}

	protected void tearDown() throws Exception {
		super.tearDown();

		delegate.tearDown();
		observable.dispose();
		observable = null;
		
		RealmTester.setDefault(previousRealm);

		observable = null;
		previousRealm = null;
	}
	
	/**
	 * Creates a new observable with a default realm. Invoked from
	 * {@link #setUp()}. Override to customize the creation of observables
	 * (e.g. specifying a different Realm).
	 * 
	 * @return observable
	 */
	protected IObservable doCreateObservable() {
		return delegate.createObservable(new CurrentRealm(true));
	}

	/**
	 * Returns the created observable. The observable is created in
	 * {@link #setUp()}. If invoked before {@link #setUp()} will be
	 * <code>null</code>.
	 * 
	 * @return observable
	 */
	protected IObservable getObservable() {
		return observable;
	}	
	
	/**
	 * Returns the delegate in use.
	 * 
	 * @return delegate
	 */
	protected IObservableContractDelegate getObservableContractDelegate() {
		return delegate;
	}
	
	protected String formatFail(String message) {
		return message + getDebugString();
	}
	
	private String getDebugString() {
		if (debugInfo == null) {
			debugInfo = "(Test: " + this.getClass().getName() + ", Delegate: " + delegate.getClass().getName() + ")";
		}
		
		return debugInfo;
	}

	/**
	 * Asserts that ObservableTracker.getterCalled(...) is invoked when the
	 * provided <code>runnable</code> is invoked.
	 * 
	 * @param runnable
	 * @param methodName
	 *            method name to display when displaying a message
	 * @param observable
	 *            observable that should be collected by ObservableTracker
	 */
	protected void assertGetterCalled(Runnable runnable, String methodName, IObservable observable) {
		IObservable[] observables = ObservableTracker.runAndMonitor(runnable,
				null, null);
	
		int count = 0;
		for (int i = 0; i < observables.length; i++) {
			if (observables[i] == observable) {
				count++;
			}
		}
		
		assertEquals(formatFail(methodName
				+ " should invoke ObservableTracker.getterCalled() once."), 1,
				count);
	}
}
