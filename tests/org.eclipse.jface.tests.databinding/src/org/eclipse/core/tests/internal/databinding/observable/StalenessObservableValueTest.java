/*******************************************************************************
 * Copyright (c) 2007, 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for StalenessObservableValue
 * 
 * @since 1.1
 */
public class StalenessObservableValueTest extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(StalenessObservableValueTest.class.getName());
		suite.addTest(ObservableValueContractTest.suite(new Delegate()));
		return suite;
	}

	static class ObservableStub extends AbstractObservable {
		boolean stale;

		public ObservableStub(Realm realm) {
			super(realm);
		}

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
		public IObservableValue createObservableValue(Realm realm) {
			return new StalenessObservableValueStub(new ObservableStub(realm));
		}

		public void change(IObservable observable) {
			ObservableStub target = ((StalenessObservableValueStub) observable).target;
			target.setStale(!target.isStale());
		}

		public Object getValueType(IObservableValue observable) {
			return Boolean.TYPE;
		}
	}
}