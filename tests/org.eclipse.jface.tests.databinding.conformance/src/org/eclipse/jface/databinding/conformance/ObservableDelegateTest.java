/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 221351
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.delegate.IObservableContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.junit.After;
import org.junit.Before;

/**
 * TestCase that provides the standard behavior expected for delegating test
 * cases.
 *
 * @since 3.2
 */
public class ObservableDelegateTest {
	private final IObservableContractDelegate delegate;

	private Realm previousRealm;

	private IObservable observable;
	private String debugInfo;

	public ObservableDelegateTest(IObservableContractDelegate delegate) {
		this.delegate = delegate;
	}

	@Before
	public void setUp() throws Exception {
		previousRealm = Realm.getDefault();

		delegate.setUp();
		observable = doCreateObservable();
	}

	@After
	public void tearDown() throws Exception {
		delegate.tearDown();
		observable.dispose();
		observable = null;

		RealmTester.setDefault(previousRealm);

		observable = null;
		previousRealm = null;
	}

	/**
	 * Creates a new observable with a default realm. Invoked from
	 * {@link #setUp()}. Override to customize the creation of observables (e.g.
	 * specifying a different Realm).
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
			debugInfo = "(Test: " + this.getClass().getName() + ", Delegate: "
					+ delegate.getClass().getName() + ")";
		}

		return debugInfo;
	}

	/**
	 * Asserts that ObservableTracker.getterCalled(...) is invoked when the
	 * provided <code>runnable</code> is invoked.
	 *
	 * @param methodName
	 *            method name to display when displaying a message
	 * @param observable
	 *            observable that should be collected by ObservableTracker
	 */
	protected void assertGetterCalled(Runnable runnable, String methodName,
			IObservable observable) {
		IObservable[] observables = ObservableTracker.runAndMonitor(runnable,
				null, null);

		int count = 0;
		for (IObservable o : observables) {
			if (o == observable) {
				count++;
			}
		}

		assertEquals(formatFail(methodName
				+ " should invoke ObservableTracker.getterCalled() once."), 1,
				count);
	}
}
