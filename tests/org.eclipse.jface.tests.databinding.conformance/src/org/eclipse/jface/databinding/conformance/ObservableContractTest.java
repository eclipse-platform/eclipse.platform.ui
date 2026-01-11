/*******************************************************************************
 * Copyright (c) 2007, 2018 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 208322, 221351, 208858, 146397, 249526
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.delegate.IObservableContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.DisposeEventTracker;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

	private final IObservableContractDelegate delegate;

	public ObservableContractTest(IObservableContractDelegate delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		observable = getObservable();
	}

	@Test
	public void testConstruction_CallsObservableCreated() {
		final IObservable[] created = new IObservable[1];
		IObservable[] collected = ObservableTracker
				.runAndCollect(() -> created[0] = delegate.createObservable(new CurrentRealm(true)));
		assertTrue(collected.length > 0);
		boolean wasCollected = false;
		for (IObservable c : collected) {
			if (c == created[0]) {
				wasCollected = true;
				break;
			}
		}
		assertTrue(wasCollected);
	}

	@Test
	public void testGetRealm_NotNull() throws Exception {
		assertNotNull(observable.getRealm(),
				formatFail("The observable's realm should not be null."));
	}

	@Test
	public void testChange_ChangeEvent() throws Exception {
		ChangeListener listener = new ChangeListener();

		observable.addChangeListener(listener);
		delegate.change(observable);

		assertEquals(1, listener.count,
				formatFail("A change in the observable should notify change listeners."));
	}

	@Test
	public void testChange_EventObservable() throws Exception {
		ChangeListener listener = new ChangeListener();

		observable.addChangeListener(listener);
		delegate.change(observable);

		ChangeEvent event = listener.event;
		assertNotNull(event, formatFail("change event was null"));

		assertSame(observable, event.getObservable(),
				formatFail("In the change event the source of the change should be the observable."));
	}

	@Test
	public void testChange_RealmCheck() throws Exception {
		RealmTester.exerciseCurrent(() -> delegate.change(observable), (CurrentRealm) observable.getRealm());
	}

	@Test
	public void testChange_ObservableRealmIsTheCurrentRealm() throws Exception {
		ChangeListener listener = new ChangeListener();
		observable.addChangeListener(listener);

		delegate.change(observable);
		assertTrue(listener.isCurrentRealm,
				formatFail("On change the current realm should be the realm of the observable."));
	}

	@Test
	public void testRemoveChangeListener_RemovesListener() throws Exception {
		ChangeListener listener = new ChangeListener();

		observable.addChangeListener(listener);
		delegate.change(observable);

		// precondition check
		assertEquals(1, listener.count, formatFail("change did not notify listeners"));

		observable.removeChangeListener(listener);
		delegate.change(observable);

		assertEquals(1, listener.count,
				formatFail("When a change listener is removed it should not still receive change events."));
	}

	@Test
	public void testIsStale_NotStale() throws Exception {
		delegate.setStale(observable, false);
		assertFalse(observable.isStale(),
				formatFail("When an observable is not stale isStale() should return false."));
	}

	@Test
	public void testIsStale_RealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> observable.isStale(), (CurrentRealm) observable.getRealm());
	}

	@Test
	public void testIsStale_GetterCalled() throws Exception {
		assertGetterCalled(() -> observable.isStale(), "isStale", observable);
	}

	@Test
	public void testIsDisposed() throws Exception {
		assertFalse(observable.isDisposed());
		observable.dispose();
		assertTrue(observable.isDisposed());
	}

	@Test
	public void testAddDisposeListener_HandleDisposeInvoked() {
		DisposeEventTracker tracker = DisposeEventTracker.observe(observable);
		assertEquals(0, tracker.count);
		observable.dispose();
		assertEquals(1, tracker.count);
		assertSame(observable, tracker.event.getSource());
	}

	@Test
	public void testHandleDispose_IsDisposedTrue() {
		// Ensures observable.isDisposed() == true before
		// the dispose listeners are called
		observable.addDisposeListener(staleEvent -> assertTrue(observable.isDisposed()));
		observable.dispose();
	}

	@Test
	public void testDispose_RemovesListeners() throws Exception {
		ChangeListener disposedObservableListener = new ChangeListener();
		Realm realm = observable.getRealm();

		observable.addChangeListener(disposedObservableListener);
		observable.dispose();

		// create a new observable to fire a change from
		observable = delegate.createObservable(realm);
		delegate.change(observable);

		assertEquals(0, disposedObservableListener.count,
				formatFail("After being disposed listeners should not receive change events."));
	}

	@Test
	public void testDispose_PreservesRealm() throws Exception {
		Realm realm = observable.getRealm();

		observable.dispose();

		assertSame(realm, observable.getRealm());
	}

	/* package */static class ChangeListener implements IChangeListener {
		int count;

		ChangeEvent event;

		boolean isCurrentRealm;

		@Override
		public void handleChange(ChangeEvent event) {
			count++;
			this.event = event;
			this.isCurrentRealm = event.getObservable().getRealm().isCurrent();
		}
	}
}
