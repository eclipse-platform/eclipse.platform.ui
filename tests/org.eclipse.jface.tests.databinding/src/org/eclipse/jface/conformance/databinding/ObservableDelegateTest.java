/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.conformance.databinding;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.tests.databinding.RealmTester;
import org.eclipse.jface.tests.databinding.RealmTester.CurrentRealm;

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
}
