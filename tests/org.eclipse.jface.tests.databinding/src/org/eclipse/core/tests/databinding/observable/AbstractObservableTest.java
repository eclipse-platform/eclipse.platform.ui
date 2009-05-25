/*******************************************************************************
 * Copyright (c) 2006, 2009 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Matthew Hall - bugs 208332, 213145, 255734
 *     Ovidio Mallo - bug 247741
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.DisposeEvent;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.jface.databinding.conformance.ObservableContractTest;
import org.eclipse.jface.databinding.conformance.ObservableStaleContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.conformance.util.StaleEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

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

		StaleEventTracker listener1 = new StaleEventTracker();

		assertFalse(observable.firstListenerAdded);
		observable.addStaleListener(listener1);
		assertTrue(observable.firstListenerAdded);
		observable.firstListenerAdded = false; // reset

		assertTrue(observable.hasListeners());
		assertEquals(0, listener1.count);

		observable.fireStale();

		assertEquals(1, listener1.count);
		assertSame(observable, listener1.event.getObservable());

		// Add a second stale listener as 1 vs. 2 listener code is different.
		StaleEventTracker listener2 = new StaleEventTracker();
		assertEquals(0, listener2.count);
		observable.addStaleListener(listener2);
		observable.fireStale();

		assertEquals(2, listener1.count);
		assertEquals(1, listener2.count);

		// Add a third stale listener as 2 vs. 3 or greater code is different.
		StaleEventTracker listener3 = new StaleEventTracker();
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
		StaleEventTracker staleListener = new StaleEventTracker();

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

	public void testAddDisposeListener_HasListenersFalse() {
		IDisposeListener disposeListener = new IDisposeListener() {
			public void handleDispose(DisposeEvent staleEvent) {
			}
		};
		IStaleListener staleListener = new IStaleListener() {
			public void handleStale(StaleEvent staleEvent) {
			}
		};

		assertFalse(observable.hasListeners());

		observable.addDisposeListener(disposeListener);
		assertFalse(observable.hasListeners());
		assertFalse(observable.firstListenerAdded);
		assertFalse(observable.lastListenerRemoved);

		observable.addStaleListener(staleListener);
		assertTrue(observable.hasListeners());
		assertTrue(observable.firstListenerAdded);
		assertFalse(observable.lastListenerRemoved);

		observable.removeDisposeListener(disposeListener);
		assertTrue(observable.hasListeners());
		assertTrue(observable.firstListenerAdded);
		assertFalse(observable.lastListenerRemoved);

		observable.removeStaleListener(staleListener);
		assertFalse(observable.hasListeners());
		assertTrue(observable.firstListenerAdded);
		assertTrue(observable.lastListenerRemoved);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AbstractObservableTest.class.getName());
		suite.addTestSuite(AbstractObservableTest.class);
		Delegate delegate = new Delegate();
		suite.addTest(ObservableContractTest.suite(delegate));
		suite.addTest(ObservableStaleContractTest.suite(delegate));
		return suite;
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

		protected void fireStale() {
			super.fireStale();
		}

		protected void fireChange() {
			super.fireChange();
		}

		public boolean isStale() {
			getterCalled();
			return stale;
		}

		private void getterCalled() {
			ObservableTracker.getterCalled(this);
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
