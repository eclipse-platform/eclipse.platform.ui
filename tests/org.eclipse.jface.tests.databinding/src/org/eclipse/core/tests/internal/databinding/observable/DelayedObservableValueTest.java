/*******************************************************************************
 * Copyright (c) 2007, 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 212223)
 *     Matthew Hall - bug 213145, 245647
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.MutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;

/**
 * Tests for DelayedObservableValue
 * 
 * @since 1.2
 */
public class DelayedObservableValueTest extends AbstractDefaultRealmTestCase {
	private Object oldValue;
	private Object newValue;
	private ObservableValueStub target;
	private IObservableValue delayed;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		target = new ObservableValueStub(Realm.getDefault());
		oldValue = new Object();
		newValue = new Object();
		target.setValue(oldValue);
		delayed = Observables.observeDelayedValue(1, target);
	}

	@Override
	protected void tearDown() throws Exception {
		target.dispose();
		target = null;
		super.tearDown();
	}

	public void testIsStale_WhenTargetIsStale() {
		assertFalse(target.isStale());
		assertFalse(delayed.isStale());

		target.fireStale();

		assertTrue(target.isStale());
		assertTrue(delayed.isStale());
	}

	public void testIsStale_DuringDelay() {
		assertFalse(target.isStale());
		assertFalse(delayed.isStale());

		target.setValue(newValue);

		assertFalse(target.isStale());
		assertTrue(delayed.isStale());
	}

	public void testGetValueType_SameAsTarget() {
		assertEquals(target.getValueType(), delayed.getValueType());
	}

	public void testGetValue_FiresPendingValueChange() {
		assertFiresPendingValueChange(new Runnable() {
			@Override
			public void run() {
				final Object value = delayed.getValue();
				assertEquals(newValue, value);
			}
		});
	}

	public void testSetValue_PropagatesToTarget() {
		assertEquals(oldValue, delayed.getValue());
		assertEquals(oldValue, target.getValue());

		delayed.setValue(newValue);

		assertEquals(newValue, target.getValue());
		assertEquals(newValue, delayed.getValue());
	}

	public void testSetValue_CachesGetValueFromTarget() {
		Object overrideValue = target.overrideValue = new Object();

		assertEquals(oldValue, delayed.getValue());
		assertEquals(oldValue, target.getValue());

		delayed.setValue(newValue);

		assertEquals(overrideValue, target.getValue());
		assertEquals(overrideValue, delayed.getValue());
	}

	public void testSetValue_FiresValueChangeEvent() {
		ValueChangeEventTracker targetTracker = ValueChangeEventTracker
				.observe(target);
		ValueChangeEventTracker delayedTracker = ValueChangeEventTracker
				.observe(delayed);

		delayed.setValue(newValue);

		assertEquals(1, targetTracker.count);
		assertEquals(oldValue, targetTracker.event.diff.getOldValue());
		assertEquals(newValue, targetTracker.event.diff.getNewValue());

		assertEquals(1, delayedTracker.count);
		assertEquals(oldValue, delayedTracker.event.diff.getOldValue());
		assertEquals(newValue, delayedTracker.event.diff.getNewValue());
	}

	public void testWait_FiresPendingValueChange() {
		assertFiresPendingValueChange(new Runnable() {
			@Override
			public void run() {
				// Give plenty of time for display to run timer task
				long timeout = time() + 5000;
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
		ValueChangeEventTracker tracker = ValueChangeEventTracker
				.observe(delayed);

		target.setValue(newValue);
		assertTrue(delayed.isStale());
		assertEquals(0, tracker.count);

		runnable.run();

		assertFalse(delayed.isStale());
		assertEquals(1, tracker.count);
		assertEquals(oldValue, tracker.event.diff.getOldValue());
		assertEquals(newValue, tracker.event.diff.getNewValue());
	}

	static class ObservableValueStub extends AbstractObservableValue {
		private Object value;
		private boolean stale;

		Object overrideValue;

		public ObservableValueStub(Realm realm) {
			super(realm);
		}

		@Override
		protected Object doGetValue() {
			return value;
		}

		@Override
		protected void doSetValue(Object value) {
			Object oldValue = this.value;
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

	public static Test suite() {
		TestSuite suite = new TestSuite(DelayedObservableValueTest.class
				.getName());
		suite.addTestSuite(DelayedObservableValueTest.class);
		suite.addTest(MutableObservableValueContractTest
				.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableValueContractDelegate {
		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return Observables.observeDelayedValue(0, new ObservableValueStub(
					realm));
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return Object.class;
		}

		@Override
		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(createValue(observableValue));
		}

		@Override
		public Object createValue(IObservableValue observable) {
			return new Object();
		}
	}
}
