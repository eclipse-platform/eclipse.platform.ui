/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.delegate.IObservableContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;

/**
 * Tests for IObservable that don't require mutating the observable.
 * <p>
 * This class is experimental and can change at any time. It is recommended to
 * not subclass or assume the test names will not change. The only API that is
 * guaranteed to not change are the constructors. The tests will remain public
 * and not final in order to allow for consumers to turn off a test if needed by
 * subclassing.
 * </p>
 * 
 * @since 3.2
 */
public class ObservableContractTest extends ObservableDelegateTest {
	private IObservable observable;

	private IObservableContractDelegate delegate;

	public ObservableContractTest(IObservableContractDelegate delegate) {
		this(null, delegate);
	}

	public ObservableContractTest(String testName,
			IObservableContractDelegate delegate) {
		super(testName, delegate);
		
		this.delegate = delegate;
	}

	protected void setUp() throws Exception {
		super.setUp();
		observable = getObservable();
	}

	public void testGetRealm_NotNull() throws Exception {
		assertNotNull(formatFail("The observable's realm should not be null."), observable
				.getRealm());
	}

	public void testChange_ChangeEvent() throws Exception {
		ChangeListener listener = new ChangeListener();

		observable.addChangeListener(listener);
		delegate.change(observable);

		assertEquals(
				formatFail("A change in the observable should notify change listeners."),
				listener.count, 1);
	}

	public void testChange_EventObservable() throws Exception {
		ChangeListener listener = new ChangeListener();

		observable.addChangeListener(listener);
		delegate.change(observable);

		ChangeEvent event = listener.event;
		assertNotNull(formatFail("change event was null"), event);

		assertSame(
				formatFail("In the change event the source of the change should be the observable."),
				observable, event.getObservable());
	}

	public void testChange_RealmCheck() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				delegate.change(observable);
			}			
		}, (CurrentRealm) observable.getRealm());
	}
	
	public void testChange_ObservableRealmIsTheCurrentRealm() throws Exception {
		ChangeListener listener = new ChangeListener();
		observable.addChangeListener(listener);

		delegate.change(observable);
		assertTrue(
				formatFail("On change the current realm should be the realm of the observable."),
				listener.isCurrentRealm);
	}

	public void testRemoveChangeListener_RemovesListener() throws Exception {
		ChangeListener listener = new ChangeListener();

		observable.addChangeListener(listener);
		delegate.change(observable);

		// precondition check
		assertEquals(formatFail("change did not notify listeners"), 1, listener.count);

		observable.removeChangeListener(listener);
		delegate.change(observable);

		assertEquals(
				formatFail("When a change listener is removed it should not still receive change events."),
				1, listener.count);
	}

	public void testIsStale_NotStale() throws Exception {
		delegate.setStale(observable, false);
		assertFalse(
				formatFail("When an observable is not stale isStale() should return false."),
				observable.isStale());
	}
	
	public void testIsStale_RealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				delegate.change(observable);
			}			
		}, (CurrentRealm) observable.getRealm());
	}

	public void testDispose_RemovesListeners() throws Exception {
		ChangeListener disposedObservableListener = new ChangeListener();
		Realm realm = observable.getRealm();
		
		observable.addChangeListener(disposedObservableListener);
		observable.dispose();
		
		//create a new observable to fire a change from
		observable = delegate.createObservable(realm);
		delegate.change(observable);
		
		assertEquals(
				formatFail("After being disposed listeners should not receive change events."),
				0, disposedObservableListener.count);
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
	protected void assertGetterCalled(Runnable runnable,
			String methodName, IObservable observable) {
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
	
	/* package */static class ChangeListener implements IChangeListener {
		int count;

		ChangeEvent event;

		boolean isCurrentRealm;

		public void handleChange(ChangeEvent event) {
			count++;
			this.event = event;
			this.isCurrentRealm = event.getObservable().getRealm().isCurrent();
		}
	}
}
