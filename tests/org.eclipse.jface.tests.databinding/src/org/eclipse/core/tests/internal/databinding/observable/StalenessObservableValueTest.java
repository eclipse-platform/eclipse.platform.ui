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

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.observable.StalenessObservableValue;
import org.eclipse.jface.databinding.conformance.ObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;

import junit.framework.TestSuite;

/**
 * Tests for StalenessObservableValue
 *
 * @since 1.1
 */
public class StalenessObservableValueTest {
	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(ObservableValueContractTest.suite(new Delegate()));
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
		public IObservableValue createObservableValue(Realm realm) {
			return new StalenessObservableValueStub(new ObservableStub(realm));
		}

		@Override
		public void change(IObservable observable) {
			ObservableStub target = ((StalenessObservableValueStub) observable).target;
			target.setStale(!target.isStale());
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return Boolean.TYPE;
		}
	}
}