/*******************************************************************************
 * Copyright (c) 2007, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 212223)
 *     Matthew Hall - bug 213145, 245647
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.MutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.TestCollection;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for DelayedObservableValue
 *
 * @since 1.2
 */
public class DelayedObservableValueTest extends AbstractDefaultRealmTestCase {
	private Object oldValue;
	private Object newValue;
	private ObservableValueStub<Object> target;
	private IObservableValue<Object> delayed;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		target = new ObservableValueStub<>(Realm.getDefault());
		oldValue = new Object();
		newValue = new Object();
		target.setValue(oldValue);
		delayed = Observables.observeDelayedValue(1, target);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		target.dispose();
		target = null;
		super.tearDown();
	}

	@Test
	public void testIsStale_WhenTargetIsStale() {
		assertFalse(target.isStale());
		assertFalse(delayed.isStale());

		target.fireStale();

		assertTrue(target.isStale());
		assertTrue(delayed.isStale());
	}

	@Test
	public void testIsStale_DuringDelay() {
		assertFalse(target.isStale());
		assertFalse(delayed.isStale());

		target.setValue(newValue);

		assertFalse(target.isStale());
		assertTrue(delayed.isStale());
	}

	@Test
	public void testGetValueType_SameAsTarget() {
		assertEquals(target.getValueType(), delayed.getValueType());
	}

	@Test
	public void testGetValue_FiresPendingValueChange() {
		assertFiresPendingValueChange(() -> {
			final Object value = delayed.getValue();
			assertEquals(newValue, value);
		});
	}

	@Test
	public void testSetValue_PropagatesToTarget() {
		assertEquals(oldValue, delayed.getValue());
		assertEquals(oldValue, target.getValue());

		delayed.setValue(newValue);

		assertEquals(newValue, target.getValue());
		assertEquals(newValue, delayed.getValue());
	}

	/**
	 * Bug 558650. Staleness state should be updated before the staleness event is
	 * fired.
	 */
	@Test
	public void testStaleListener() {
		AtomicInteger nrEvents = new AtomicInteger();

		delayed.addStaleListener(e -> {
			nrEvents.incrementAndGet();
			// Staleness state should be updated before firing event
			assertTrue(delayed.isStale());
		});

		target.setValue(newValue);

		assertTrue(delayed.isStale());
		assertEquals(1, nrEvents.get());

		delayed.getValue();
		assertFalse(delayed.isStale());

		// There is no non-stale event so nrEvents is not incremented
		assertEquals(1, nrEvents.get());
	}

	/**
	 * Bug 525894. The target observable should not be captured by
	 * {@link ObservableTracker}. That results in that the value change is observed
	 * without the delay.
	 */
	@Test
	public void testInnerObservableNotTracked() {
		// Make the target dirty
		target.setValue("test");

		List<IObservable> tracked = Arrays.asList(ObservableTracker.runAndMonitor(() -> {
			delayed.getValue();
		}, null, null));

		assertFalse(tracked.contains(target));
	}

	@Test
	public void testSetValue_CachesGetValueFromTarget() {
		Object overrideValue = target.overrideValue = new Object();

		assertEquals(oldValue, delayed.getValue());
		assertEquals(oldValue, target.getValue());

		delayed.setValue(newValue);

		assertEquals(overrideValue, target.getValue());
		assertEquals(overrideValue, delayed.getValue());
	}

	@Test
	public void testSetValue_FiresValueChangeEvent() {
		ValueChangeEventTracker<?> targetTracker = ValueChangeEventTracker.observe(target);
		ValueChangeEventTracker<?> delayedTracker = ValueChangeEventTracker.observe(delayed);

		delayed.setValue(newValue);

		assertEquals(1, targetTracker.count);
		assertEquals(oldValue, targetTracker.event.diff.getOldValue());
		assertEquals(newValue, targetTracker.event.diff.getNewValue());

		assertEquals(1, delayedTracker.count);
		assertEquals(oldValue, delayedTracker.event.diff.getOldValue());
		assertEquals(newValue, delayedTracker.event.diff.getNewValue());
	}

	@Test
	public void testWait_FiresPendingValueChange() {
		assertFiresPendingValueChange(new Runnable() {
			@Override
			public void run() {
				// Give plenty of time for display to run timer task
				long timeout = time() + 20_000;
				do {
					sleep(10);
					processDisplayEvents();
				} while (delayed.isStale() && time() < timeout);
			}

			private void sleep(int delay) {
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			private void processDisplayEvents() {
				Display display = Display.getCurrent();
				while (display.readAndDispatch()) {
				}
			}

			private long time() {
				return System.currentTimeMillis();
			}
		});
	}

	private void assertFiresPendingValueChange(Runnable runnable) {
		ValueChangeEventTracker<?> tracker = ValueChangeEventTracker.observe(delayed);

		target.setValue(newValue);
		assertTrue(delayed.isStale());
		assertEquals(0, tracker.count);

		runnable.run();

		assertFalse(delayed.isStale());
		assertEquals(1, tracker.count);
		assertEquals(oldValue, tracker.event.diff.getOldValue());
		assertEquals(newValue, tracker.event.diff.getNewValue());
	}

	static class ObservableValueStub<T> extends AbstractObservableValue<T> {
		private T value;
		private boolean stale;

		T overrideValue;

		public ObservableValueStub(Realm realm) {
			super(realm);
		}

		@Override
		protected T doGetValue() {
			return value;
		}

		@Override
		protected void doSetValue(T value) {
			T oldValue = this.value;
			if (overrideValue != null)
				value = overrideValue;
			this.value = value;
			stale = false;
			fireValueChange(Diffs.createValueDiff(oldValue, value));
		}

		@Override
		public Object getValueType() {
			return Object.class;
		}

		@Override
		protected void fireStale() {
			stale = true;
			super.fireStale();
		}

		@Override
		public boolean isStale() {
			return stale;
		}
	}

	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(MutableObservableValueContractTest.class, new Delegate());
	}

	static class Delegate extends AbstractObservableValueContractDelegate {
		@Override
		public IObservableValue<?> createObservableValue(Realm realm) {
			return Observables.observeDelayedValue(0, new ObservableValueStub<>(realm));
		}

		@Override
		public Object getValueType(IObservableValue<?> observable) {
			return Object.class;
		}

		@Override
		public void change(IObservable observable) {
			@SuppressWarnings("unchecked")
			IObservableValue<Object> observableValue = (IObservableValue<Object>) observable;
			observableValue.setValue(createValue(observableValue));
		}

		@Override
		public Object createValue(IObservableValue<?> observable) {
			return new Object();
		}
	}
}
