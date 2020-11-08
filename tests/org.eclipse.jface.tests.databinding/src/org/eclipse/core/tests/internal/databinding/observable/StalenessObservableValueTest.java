/*******************************************************************************
 * Copyright (c) 2007, 2008 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 212468)
 *     Matthew Hall - bug 213145
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.StalenessObservableValue;
import org.eclipse.jface.databinding.conformance.ObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.TestCollection;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

/**
 * Tests for StalenessObservableValue
 *
 * @since 1.1
 */
public class StalenessObservableValueTest extends AbstractDefaultRealmTestCase {

	/**
	 * Test that {@link StalenessObservableValue#getValue} returns the new value
	 * when called inside the value change listener.
	 */
	@Test
	public void valueDuringListenerCallback() {
		IObservableValue<String> source = new WritableValue<>("a", String.class);
		IObservableValue<String> delayed = Observables.observeDelayedValue(1, source);
		IObservableValue<Boolean> stale = Observables.observeStale(delayed);

		AtomicInteger nrStaleEvents = new AtomicInteger();

		stale.addValueChangeListener(e -> {
			if (nrStaleEvents.incrementAndGet() == 1) {
				assertTrue(stale.getValue());
				assertTrue(e.diff.getNewValue());
				assertFalse(e.diff.getOldValue());
			} else {
				assertFalse(stale.getValue());
				assertFalse(e.diff.getNewValue());
				assertTrue(e.diff.getOldValue());
			}
		});

		source.setValue("b");

		// Makes the observable non-stale
		delayed.getValue();

		assertEquals(2, nrStaleEvents.get());
	}

	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(ObservableValueContractTest.class, new Delegate());
	}

	static class ObservableStub extends AbstractObservable {
		boolean stale;

		public ObservableStub(Realm realm) {
			super(realm);
		}

		@Override
		public boolean isStale() {
			return stale;
		}

		public void setStale(boolean stale) {
			if (this.stale == stale)
				return;

			this.stale = stale;
			if (stale) {
				fireStale();
			} else {
				fireChange();
			}
		}
	}

	static class StalenessObservableValueStub extends StalenessObservableValue {
		ObservableStub target;

		StalenessObservableValueStub(ObservableStub target) {
			super(target);
			this.target = target;
		}
	}

	static class Delegate extends AbstractObservableValueContractDelegate {
		@Override
		public IObservableValue<?> createObservableValue(Realm realm) {
			return new StalenessObservableValueStub(new ObservableStub(realm));
		}

		@Override
		public void change(IObservable observable) {
			ObservableStub target = ((StalenessObservableValueStub) observable).target;
			target.setStale(!target.isStale());
		}

		@Override
		public Object getValueType(IObservableValue<?> observable) {
			return Boolean.TYPE;
		}
	}
}