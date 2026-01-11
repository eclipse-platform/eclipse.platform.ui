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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.jface.databinding.conformance.delegate.IObservableContractDelegate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		observable = getObservable();
	}

	@Test
	public void testIsStale_TrueWhenStale() throws Exception {
		delegate.setStale(observable, true);
		assertTrue(observable.isStale(),
				formatFail("When stale isStale() should return true."));
	}

	@Test
	public void testIsStale_FalseWhenNotStale() throws Exception {
		delegate.setStale(observable, false);
		assertFalse(observable.isStale(),
				formatFail("When not stale isStale() should return false."));
	}

	@Test
	public void testBecomingStaleFiresStaleEvent() throws Exception {
		StaleListener listener = new StaleListener();

		// precondition
		ensureStale(observable, false);

		observable.addStaleListener(listener);
		delegate.setStale(observable, true);

		assertEquals(1, listener.count,
				formatFail("When becoming stale listeners should be notified."));
	}

	@Test
	public void testStaleEventObservable() throws Exception {
		StaleListener listener = new StaleListener();

		// precondition
		ensureStale(observable, false);

		observable.addStaleListener(listener);
		delegate.setStale(observable, true);

		StaleEvent event = listener.event;
		assertNotNull(event, formatFail("stale event was null"));
		assertEquals(observable, event.getObservable(),
				formatFail("When notifying listeners of becoming stale the observable should be the source of the event."));
	}

	@Test
	public void testRemoveStaleListener_RemovesListener() throws Exception {
		StaleListener listener = new StaleListener();

		observable.addStaleListener(listener);
		ensureStale(observable, false);
		delegate.setStale(observable, true);

		// precondition check
		assertEquals(1, listener.count, formatFail("set stale did not notify listeners"));

		observable.removeStaleListener(listener);
		ensureStale(observable, false);
		delegate.setStale(observable, true);

		assertEquals(1, listener.count,
				formatFail("Once removed stale listeners should not be notified of becoming stale."));
	}

	@Test
	public void testStaleListenersAreNotNotifiedWhenObservableIsNoLongerStale()
			throws Exception {
		ensureStale(observable, true);

		StaleListener listener = new StaleListener();
		observable.addStaleListener(listener);
		delegate.setStale(observable, false);

		assertEquals(0, listener.count,
				formatFail("Stale listeners should not be notified when the stale state changes from true to false."));
	}

	@Test
	public void testObservableRealmIsCurrentOnStale() throws Exception {
		ensureStale(observable, false);

		StaleListener listener = new StaleListener();
		observable.addStaleListener(listener);
		delegate.setStale(observable, true);

		assertTrue(listener.isCurrentRealm,
				formatFail("When notifying listeners of becoming stale the observable's realm should be the current realm."));
	}

	/**
	 * Ensures that stale is set to the provided state. Will throw an
	 * AssertionFailedError if setting of the state is unsuccessful.
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
