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

package org.eclipse.jface.tests.internal.databinding.provisional.observable;

import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.util.RealmTester;
import org.eclipse.jface.tests.databinding.util.RealmTester.CurrentRealm;
import org.eclipse.swt.widgets.Display;

/**
 * Tests for AbstractObservable.
 * 
 * @since 1.1
 */
public class AbstractObservableTest extends TestCase {
	private ObservableStub observable;
	
	protected void setUp() throws Exception {
        Realm.setDefault(SWTObservables.getRealm(Display.getDefault()));
		observable = new ObservableStub(Realm.getDefault());
	}
	
	protected void tearDown() throws Exception {
		Realm.setDefault(null);
	}
	
	public void testStaleListener() throws Exception {
		assertFalse(observable.hasListeners());
		
		StaleListener listener1 = new StaleListener();
		
		assertFalse(observable.firstListenerAdded);
		observable.addStaleListener(listener1);
		assertTrue(observable.firstListenerAdded);
		observable.firstListenerAdded = false; //reset
		
		assertTrue(observable.hasListeners());
		assertEquals(0, listener1.count);
		
		observable.fireStale();
		
		assertEquals(1, listener1.count);
		assertSame(observable, listener1.source);
		
		//Add a second stale listener as 1 vs. 2 listener code is different.
		StaleListener listener2 = new StaleListener();
		assertEquals(0, listener2.count);
		observable.addStaleListener(listener2);
		observable.fireStale();
		
		assertEquals(2, listener1.count);
		assertEquals(1, listener2.count);
		
		//Add a third stale listener as 2 vs. 3 or greater code is different.
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
		
		ChangeListener listener1 = new ChangeListener();
		
		assertFalse(observable.firstListenerAdded);
		observable.addChangeListener(listener1);
		assertTrue(observable.firstListenerAdded);
		observable.firstListenerAdded = false;
		
		assertTrue(observable.hasListeners());
		assertEquals(0, listener1.count);
		
		observable.fireChange();
		
		assertEquals(1, listener1.count);
		assertSame(observable, listener1.source);
		
		//Add a second listener as the 1 vs. 2 listener code is different.
		ChangeListener listener2 = new ChangeListener();
		observable.addChangeListener(listener2);
		assertEquals(0, listener2.count);
		
		observable.fireChange();
		assertEquals(2, listener1.count);
		assertEquals(1, listener2.count);
		
		//Add a third listener as the 2 vs. 3 or greater code is different.
		ChangeListener listener3 = new ChangeListener();
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
		ChangeListener changeListener = new ChangeListener();
		StaleListener staleListener = new StaleListener();
		
		assertFalse(observable.hasListeners());
		assertFalse(observable.firstListenerAdded);
		assertFalse(observable.lastListenerRemoved);
		
		observable.addChangeListener(changeListener);
		assertTrue(observable.hasListeners());
		assertTrue(observable.firstListenerAdded);
		assertFalse(observable.lastListenerRemoved);
		
		//reset
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
		Realm.setDefault(new CurrentRealm(true));
		
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				observable = new ObservableStub();
				observable.fireStale();
			}			
		});
	}
	
	public void testFireChangeRealmChecks() throws Exception {
		Realm.setDefault(new CurrentRealm(true));
		
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				observable = new ObservableStub();
				observable.fireChange();
			}
		});
	}
	
	private class ChangeListener implements IChangeListener {
		int count;
		IObservable source;
		
		public void handleChange(IObservable source) {
			count++;
			this.source = source;
		}
	}
	
	private class StaleListener implements IStaleListener {
		int count;
		IObservable source;
		
		public void handleStale(IObservable source) {
			count++;
			this.source = source;
		}
	}
	
	private static class ObservableStub extends AbstractObservable {
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
			return false;
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
