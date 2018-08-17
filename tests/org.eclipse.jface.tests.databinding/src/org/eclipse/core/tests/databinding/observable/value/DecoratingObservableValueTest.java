/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.value;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.DecoratingObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.conformance.MutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;

import junit.framework.TestSuite;

/**
 * @since 3.2
 *
 */
public class DecoratingObservableValueTest {
	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(MutableObservableValueContractTest.suite(new Delegate()));
	}

	static class Delegate extends AbstractObservableValueContractDelegate {
		private Object valueType = Object.class;

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			IObservableValue decorated = new WritableValue(realm, new Object(),
					valueType);
			return new DecoratingObservableValueStub(decorated);
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return valueType;
		}

		@Override
		public void change(IObservable observable) {
			((DecoratingObservableValueStub) observable).decorated
					.setValue(new Object());
		}
	}

	static class DecoratingObservableValueStub extends
			DecoratingObservableValue {
		IObservableValue decorated;

		DecoratingObservableValueStub(IObservableValue decorated) {
			super(decorated, true);
			this.decorated = decorated;
		}
	}
}
