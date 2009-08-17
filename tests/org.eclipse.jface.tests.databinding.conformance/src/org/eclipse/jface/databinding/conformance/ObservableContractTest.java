/*******************************************************************************
 * Copyright (c) 2007, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 208322, 221351, 208858, 146397, 249526
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.delegate.IObservableContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.DisposeEventTracker;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;

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

	public void testConstruction_CallsObservableCreated() {
		final IObservable[] created = new IObservable[1];
		IObservable[] collected = ObservableTracker.runAndCollect(new Runnable() {
			public void run() {
				created[0] = delegate.createObservable(new CurrentRealm(true));
			}
		});
		assertTrue(collected.length > 0);
		boolean wasCollected = false;
		for (int i = 0; i < collected.length; i++) {
			if (collected[i] == created[0])
				wasCollected = true;
		}
		assertTrue(wasCollected);
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
				1, listener.count);
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
				observable.isStale();
			}
		}, (CurrentRealm) observable.getRealm());
	}

	public void testIsStale_GetterCalled() throws Exception {
		assertGetterCalled(new Runnable() {
			public void run() {
				observable.isStale();
			}
		}, "isStale", observable);
	}

	public void testIsDisposed() throws Exception {
		assertFalse(observable.isDisposed());
		observable.dispose();
		assertTrue(observable.isDisposed());
	}

	public void testAddDisposeListener_HandleDisposeInvoked() {
		DisposeEventTracker tracker = DisposeEventTracker.observe(observable);
		assertEquals(0, tracker.count);
		observable.dispose();
		assertEquals(1, tracker.count);
		assertSame(observable, tracker.event.getSource());
	}

	public void testHandleDispose_IsDisposedTrue() {
		// Ensures observable.isDisposed() == true before
		// the dispose listeners are called
		observable.addDisposeListener(new IDisposeListener() {
			public void handleDispose(DisposeEvent staleEvent) {
				assertTrue(observable.isDisposed());
			}
		});
		observable.dispose();
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

	public void testDispose_PreservesRealm() throws Exception {
		Realm realm = observable.getRealm();

		observable.dispose();

		assertSame(realm, observable.getRealm());
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

	public static Test suite(IObservableContractDelegate delegate) {
		return new SuiteBuilder().addObservableContractTest(
				ObservableContractTest.class, delegate).build();
	}
}
