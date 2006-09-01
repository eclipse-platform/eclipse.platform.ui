/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.provisional.observable;

import junit.framework.TestCase;

import org.eclipse.jface.databinding.observable.AbstractObservable;
import org.eclipse.jface.databinding.observable.IChangeListener;
import org.eclipse.jface.databinding.observable.IObservable;
import org.eclipse.jface.databinding.observable.IStaleListener;

/**
 * Tests for AbstractObservable.
 * 
 * @since 1.1
 */
public class AbstractObservableTest extends TestCase {
	private ObservableStub stub;
	
	protected void setUp() throws Exception {
		stub = new ObservableStub();
	}
	
	public void testStaleListener() throws Exception {
		assertFalse(stub.hasListeners());
		
		StaleListener listener1 = new StaleListener();
		
		assertFalse(stub.firstListenerAdded);
		stub.addStaleListener(listener1);
		assertTrue(stub.firstListenerAdded);
		stub.firstListenerAdded = false; //reset
		
		assertTrue(stub.hasListeners());
		assertEquals(0, listener1.count);
		
		stub.fireStale();
		
		assertEquals(1, listener1.count);
		assertSame(stub, listener1.source);
		
		//Add a second stale listener as 1 vs. 2 listener code is different.
		StaleListener listener2 = new StaleListener();
		assertEquals(0, listener2.count);
		stub.addStaleListener(listener2);
		stub.fireStale();
		
		assertEquals(2, listener1.count);
		assertEquals(1, listener2.count);
		
		//Add a third stale listener as 2 vs. 3 or greater code is different.
		StaleListener listener3 = new StaleListener();
		stub.addStaleListener(listener3);
		assertEquals(0, listener3.count);
		
		stub.fireStale();
		
		assertEquals(3, listener1.count);
		assertEquals(2, listener2.count);
		assertEquals(1, listener3.count);
		
		assertFalse(stub.lastListenerRemoved);
		stub.removeStaleListener(listener1);
		stub.removeStaleListener(listener2);
		stub.removeStaleListener(listener3);
		assertTrue(stub.lastListenerRemoved);
	
		assertFalse(stub.hasListeners());
	}
	
	public void testChangeListener() throws Exception {
		assertFalse(stub.hasListeners());
		
		ChangeListener listener1 = new ChangeListener();
		
		assertFalse(stub.firstListenerAdded);
		stub.addChangeListener(listener1);
		assertTrue(stub.firstListenerAdded);
		stub.firstListenerAdded = false;
		
		assertTrue(stub.hasListeners());
		assertEquals(0, listener1.count);
		
		stub.fireChange();
		
		assertEquals(1, listener1.count);
		assertSame(stub, listener1.source);
		
		//Add a second listener as the 1 vs. 2 listener code is different.
		ChangeListener listener2 = new ChangeListener();
		stub.addChangeListener(listener2);
		assertEquals(0, listener2.count);
		
		stub.fireChange();
		assertEquals(2, listener1.count);
		assertEquals(1, listener2.count);
		
		//Add a third listener as the 2 vs. 3 or greater code is different.
		ChangeListener listener3 = new ChangeListener();
		stub.addChangeListener(listener3);
		assertEquals(0, listener3.count);
		
		stub.fireChange();
		
		assertEquals(3, listener1.count);
		assertEquals(2, listener2.count);
		assertEquals(1, listener3.count);
		
		assertFalse(stub.lastListenerRemoved);
		stub.removeChangeListener(listener1);
		stub.removeChangeListener(listener2);
		stub.removeChangeListener(listener3);
		assertTrue(stub.lastListenerRemoved);
		
		assertFalse(stub.hasListeners());
	}
	
	public void testHasListenersWithChangeAndStaleListeners() throws Exception {
		ChangeListener changeListener = new ChangeListener();
		StaleListener staleListener = new StaleListener();
		
		assertFalse(stub.hasListeners());
		assertFalse(stub.firstListenerAdded);
		assertFalse(stub.lastListenerRemoved);
		
		stub.addChangeListener(changeListener);
		assertTrue(stub.hasListeners());
		assertTrue(stub.firstListenerAdded);
		assertFalse(stub.lastListenerRemoved);
		
		//reset
		stub.firstListenerAdded = false;
		stub.lastListenerRemoved = false;
		
		stub.addStaleListener(staleListener);
		assertTrue(stub.hasListeners());
		assertFalse(stub.firstListenerAdded);
		assertFalse(stub.lastListenerRemoved);
		
		stub.removeChangeListener(changeListener);
		assertTrue(stub.hasListeners());
		assertFalse(stub.firstListenerAdded);
		assertFalse(stub.lastListenerRemoved);
		
		stub.removeStaleListener(staleListener);
		assertFalse(stub.hasListeners());
		assertFalse(stub.firstListenerAdded);
		assertTrue(stub.lastListenerRemoved);
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
