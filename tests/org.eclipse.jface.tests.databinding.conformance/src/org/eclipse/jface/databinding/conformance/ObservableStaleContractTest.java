/*******************************************************************************
 * Copyright (c) 2007, 2014 Brad Reynolds and others.
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
 *     Matthew Hall - bug 213145
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 444829
 ******************************************************************************/

package org.eclipse.jface.databinding.conformance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.jface.databinding.conformance.delegate.IObservableContractDelegate;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.3
 */
public class ObservableStaleContractTest extends ObservableDelegateTest {
	private final IObservableContractDelegate delegate;
	private IObservable observable;

	public ObservableStaleContractTest(IObservableContractDelegate delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		observable = getObservable();
	}

	@Test
	public void testIsStale_TrueWhenStale() throws Exception {
		delegate.setStale(observable, true);
		assertTrue(formatFail("When stale isStale() should return true."),
				observable.isStale());
	}

	@Test
	public void testIsStale_FalseWhenNotStale() throws Exception {
		delegate.setStale(observable, false);
		assertFalse(
				formatFail("When not stale isStale() should return false."),
				observable.isStale());
	}

	@Test
	public void testBecomingStaleFiresStaleEvent() throws Exception {
		StaleListener listener = new StaleListener();

		// precondition
		ensureStale(observable, false);

		observable.addStaleListener(listener);
		delegate.setStale(observable, true);

		assertEquals(
				formatFail("When becoming stale listeners should be notified."),
				1, listener.count);
	}

	@Test
	public void testStaleEventObservable() throws Exception {
		StaleListener listener = new StaleListener();

		// precondition
		ensureStale(observable, false);

		observable.addStaleListener(listener);
		delegate.setStale(observable, true);

		StaleEvent event = listener.event;
		assertNotNull(formatFail("stale event was null"), event);
		assertEquals(
				formatFail("When notifying listeners of becoming stale the observable should be the source of the event."),
				observable, event.getObservable());
	}

	@Test
	public void testRemoveStaleListener_RemovesListener() throws Exception {
		StaleListener listener = new StaleListener();

		observable.addStaleListener(listener);
		ensureStale(observable, false);
		delegate.setStale(observable, true);

		// precondition check
		assertEquals(formatFail("set stale did not notify listeners"), 1,
				listener.count);

		observable.removeStaleListener(listener);
		ensureStale(observable, false);
		delegate.setStale(observable, true);

		assertEquals(
				formatFail("Once removed stale listeners should not be notified of becoming stale."),
				1, listener.count);
	}

	@Test
	public void testStaleListenersAreNotNotifiedWhenObservableIsNoLongerStale()
			throws Exception {
		ensureStale(observable, true);

		StaleListener listener = new StaleListener();
		observable.addStaleListener(listener);
		delegate.setStale(observable, false);

		assertEquals(
				formatFail("Stale listeners should not be notified when the stale state changes from true to false."),
				0, listener.count);
	}

	@Test
	public void testObservableRealmIsCurrentOnStale() throws Exception {
		ensureStale(observable, false);

		StaleListener listener = new StaleListener();
		observable.addStaleListener(listener);
		delegate.setStale(observable, true);

		assertTrue(
				formatFail("When notifying listeners of becoming stale the observable's realm should be the current realm."),
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

		@Override
		public void handleStale(StaleEvent staleEvent) {
			count++;
			this.event = staleEvent;
			this.isCurrentRealm = staleEvent.getObservable().getRealm()
					.isCurrent();
		}
	}
}
