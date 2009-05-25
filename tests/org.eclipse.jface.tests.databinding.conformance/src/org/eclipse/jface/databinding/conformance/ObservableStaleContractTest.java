/*******************************************************************************
 * Copyright (c) 2007, 2008 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 213145
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.jface.databinding.conformance.delegate.IObservableContractDelegate;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;

/**
 * @since 3.3
 */
public class ObservableStaleContractTest extends ObservableDelegateTest {
	private IObservableContractDelegate delegate;
	private IObservable observable;
	
	public ObservableStaleContractTest(IObservableContractDelegate delegate) {
		this(null, delegate);
	}
	
	public ObservableStaleContractTest(String testName, IObservableContractDelegate delegate) {
		super(testName, delegate);
		this.delegate = delegate;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		
		observable = getObservable();
	}
	
	public void testIsStale_TrueWhenStale() throws Exception {
		delegate.setStale(observable, true);
		assertTrue(formatFail("When stale isStale() should return true."), observable.isStale());
	}
	
	public void testIsStale_FalseWhenNotStale() throws Exception {
		delegate.setStale(observable, false);
		assertFalse(formatFail("When not stale isStale() should return false."), observable.isStale());
	}

	public void testBecomingStaleFiresStaleEvent() throws Exception {
		StaleListener listener = new StaleListener();

		// precondition
		ensureStale(observable, false);

		observable.addStaleListener(listener);
		delegate.setStale(observable, true);

		assertEquals(formatFail("When becoming stale listeners should be notified."), 1, listener.count);
	}

	public void testStaleEventObservable() throws Exception {
		StaleListener listener = new StaleListener();

		// precondition
		ensureStale(observable, false);

		observable.addStaleListener(listener);
		delegate.setStale(observable, true);

		StaleEvent event = listener.event;
		assertNotNull(formatFail("stale event was null"), event);
		assertEquals(formatFail("When notifying listeners of becoming stale the observable should be the source of the event."), observable,
				event.getObservable());
	}

	public void testRemoveStaleListener_RemovesListener() throws Exception {
		StaleListener listener = new StaleListener();

		observable.addStaleListener(listener);
		ensureStale(observable, false);
		delegate.setStale(observable, true);

		// precondition check
		assertEquals(formatFail("set stale did not notify listeners"), 1, listener.count);

		observable.removeStaleListener(listener);
		ensureStale(observable, false);
		delegate.setStale(observable, true);

		assertEquals(formatFail("Once removed stale listeners should not be notified of becoming stale."), 1,
				listener.count);
	}

	public void testStaleListenersAreNotNotifiedWhenObservableIsNoLongerStale()
			throws Exception {
		ensureStale(observable, true);

		StaleListener listener = new StaleListener();
		observable.addStaleListener(listener);
		delegate.setStale(observable, false);

		assertEquals(formatFail("Stale listeners should not be notified when the stale state changes from true to false."), 0,
				listener.count);
	}

	public void testObservableRealmIsCurrentOnStale() throws Exception {
		ensureStale(observable, false);

		StaleListener listener = new StaleListener();
		observable.addStaleListener(listener);
		delegate.setStale(observable, true);

		assertTrue(formatFail("When notifying listeners of becoming stale the observable's realm should be the current realm."),
				listener.isCurrentRealm);
	}
	
	/**
	 * Ensures that stale is set to the provided state. Will throw an
	 * AssertionFailedError if setting of the state is unsuccessful.
	 * 
	 * @param observable
	 * @param stale
	 */
	private void ensureStale(IObservable observable, boolean stale) {
		if (observable.isStale() != stale) {
			delegate.setStale(observable, stale);
		}

		assertEquals(stale, observable.isStale());
	}
	
	/* package */static class StaleListener implements IStaleListener {
		int count;

		StaleEvent event;

		boolean isCurrentRealm;

		public void handleStale(StaleEvent staleEvent) {
			count++;
			this.event = staleEvent;
			this.isCurrentRealm = staleEvent.getObservable().getRealm()
					.isCurrent();
		}
	}

	public static Test suite(IObservableContractDelegate delegate) {
		return new SuiteBuilder().addObservableContractTest(
				ObservableStaleContractTest.class, delegate).build();
	}
}
