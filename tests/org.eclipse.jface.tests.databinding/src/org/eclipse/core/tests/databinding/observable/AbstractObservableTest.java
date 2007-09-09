/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.jface.conformance.databinding.AbstractObservableContractDelegate;
import org.eclipse.jface.conformance.databinding.ObservableContractTest;
import org.eclipse.jface.conformance.databinding.ObservableStaleContractTest;
import org.eclipse.jface.conformance.databinding.SuiteBuilder;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.tests.databinding.RealmTester;
import org.eclipse.jface.tests.databinding.EventTrackers.ChangeEventTracker;
import org.eclipse.jface.tests.databinding.RealmTester.CurrentRealm;

/**
 * Tests for AbstractObservable.
 * 
 * @since 1.1
 */
public class AbstractObservableTest extends AbstractDefaultRealmTestCase {
	private ObservableStub observable;

	protected void setUp() throws Exception {
		super.setUp();
		observable = new ObservableStub(Realm.getDefault());
	}

	public void testStaleListener() throws Exception {
		assertFalse(observable.hasListeners());

		StaleListener listener1 = new StaleListener();

		assertFalse(observable.firstListenerAdded);
		observable.addStaleListener(listener1);
		assertTrue(observable.firstListenerAdded);
		observable.firstListenerAdded = false; // reset

		assertTrue(observable.hasListeners());
		assertEquals(0, listener1.count);

		observable.fireStale();

		assertEquals(1, listener1.count);
		assertSame(observable, listener1.source);

		// Add a second stale listener as 1 vs. 2 listener code is different.
		StaleListener listener2 = new StaleListener();
		assertEquals(0, listener2.count);
		observable.addStaleListener(listener2);
		observable.fireStale();

		assertEquals(2, listener1.count);
		assertEquals(1, listener2.count);

		// Add a third stale listener as 2 vs. 3 or greater code is different.
		StaleListener listener3 = new StaleListener();
		observable.addStaleListener(listener3);
		assertEquals(0, listener3.count);

		observable.fireStale();

		assertEquals(3, listener1.count);
		assertEquals(2, listener2.count);
		assertEquals(1, listener3.count);

		assertFalse(observable.lastListenerRemoved);
		observable.removeStaleListener(listener1);
		observable.removeStaleListener(listener2);
		observable.removeStaleListener(listener3);
		assertTrue(observable.lastListenerRemoved);

		assertFalse(observable.hasListeners());
	}

	public void testChangeListener() throws Exception {
		assertFalse(observable.hasListeners());

		ChangeEventTracker listener1 = new ChangeEventTracker();

		assertFalse(observable.firstListenerAdded);
		observable.addChangeListener(listener1);
		assertTrue(observable.firstListenerAdded);
		observable.firstListenerAdded = false;

		assertTrue(observable.hasListeners());
		assertEquals(0, listener1.count);

		observable.fireChange();

		assertEquals(1, listener1.count);
		assertSame(observable, listener1.event.getSource());

		// Add a second listener as the 1 vs. 2 listener code is different.
		ChangeEventTracker listener2 = new ChangeEventTracker();
		observable.addChangeListener(listener2);
		assertEquals(0, listener2.count);

		observable.fireChange();
		assertEquals(2, listener1.count);
		assertEquals(1, listener2.count);

		// Add a third listener as the 2 vs. 3 or greater code is different.
		ChangeEventTracker listener3 = new ChangeEventTracker();
		observable.addChangeListener(listener3);
		assertEquals(0, listener3.count);

		observable.fireChange();

		assertEquals(3, listener1.count);
		assertEquals(2, listener2.count);
		assertEquals(1, listener3.count);

		assertFalse(observable.lastListenerRemoved);
		observable.removeChangeListener(listener1);
		observable.removeChangeListener(listener2);
		observable.removeChangeListener(listener3);
		assertTrue(observable.lastListenerRemoved);

		assertFalse(observable.hasListeners());
	}

	public void testHasListenersWithChangeAndStaleListeners() throws Exception {
		ChangeEventTracker changeListener = new ChangeEventTracker();
		StaleListener staleListener = new StaleListener();

		assertFalse(observable.hasListeners());
		assertFalse(observable.firstListenerAdded);
		assertFalse(observable.lastListenerRemoved);

		observable.addChangeListener(changeListener);
		assertTrue(observable.hasListeners());
		assertTrue(observable.firstListenerAdded);
		assertFalse(observable.lastListenerRemoved);

		// reset
		observable.firstListenerAdded = false;
		observable.lastListenerRemoved = false;

		observable.addStaleListener(staleListener);
		assertTrue(observable.hasListeners());
		assertFalse(observable.firstListenerAdded);
		assertFalse(observable.lastListenerRemoved);

		observable.removeChangeListener(changeListener);
		assertTrue(observable.hasListeners());
		assertFalse(observable.firstListenerAdded);
		assertFalse(observable.lastListenerRemoved);

		observable.removeStaleListener(staleListener);
		assertFalse(observable.hasListeners());
		assertFalse(observable.firstListenerAdded);
		assertTrue(observable.lastListenerRemoved);
	}

	public void testFireStaleRealmChecks() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));

		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				observable = new ObservableStub();
				observable.fireStale();
			}
		});
	}

	public void testFireChangeRealmChecks() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));

		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				observable = new ObservableStub();
				observable.fireChange();
			}
		});
	}

	private class StaleListener implements IStaleListener {
		int count;
		IObservable source;

		public void handleStale(StaleEvent event) {
			count++;
			this.source = event.getObservable();
		}
	}

	public static Test suite() {
		Delegate delegate = new Delegate();

		return new SuiteBuilder()
				.addTests(AbstractObservableTest.class)
				.addObservableContractTest(ObservableContractTest.class, delegate)
				.addObservableContractTest(ObservableStaleContractTest.class, delegate)
				.build();
	}

	/* package */static class Delegate extends
			AbstractObservableContractDelegate {

		public void change(IObservable observable) {
			((ObservableStub) observable).fireChange();
		}

		public void setStale(IObservable observable, boolean stale) {
			((ObservableStub) observable).setStale(stale);
		}

		public IObservable createObservable(Realm realm) {
			return new ObservableStub(realm);
		}
	}

	private static class ObservableStub extends AbstractObservable {
		private boolean stale;

		public ObservableStub() {
			this(Realm.getDefault());
		}

		/**
		 * @param realm
		 */
		public ObservableStub(Realm realm) {
			super(realm);
		}

		private boolean firstListenerAdded;

		private boolean lastListenerRemoved;

		protected Object doGetValue() {
			return null;
		}

		public Object getValueType() {
			return null;
		}

		protected void fireStale() {
			super.fireStale();
		}

		protected void fireChange() {
			super.fireChange();
		}

		public boolean isStale() {
			return stale;
		}

		public void setStale(boolean stale) {
			boolean old = this.stale;
			this.stale = stale;

			if (stale && !old) {
				fireStale();
			}
		}

		protected boolean hasListeners() {
			return super.hasListeners();
		}

		protected void firstListenerAdded() {
			firstListenerAdded = true;
		}

		protected void lastListenerRemoved() {
			lastListenerRemoved = true;
		}
	}
}
