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
import org.eclipse.jface.databinding.conformance.util.TestCollection;

/**
 * @since 3.2
 */
public class DecoratingObservableValueTest {
	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(MutableObservableValueContractTest.class, new Delegate());
	}

	static class Delegate extends AbstractObservableValueContractDelegate {
		private final Object valueType = Object.class;

		@Override
		public IObservableValue<?> createObservableValue(Realm realm) {
			IObservableValue<Object> decorated = new WritableValue<>(realm, new Object(),
					valueType);
			return new DecoratingObservableValueStub(decorated);
		}

		@Override
		public Object getValueType(IObservableValue<?> observable) {
			return valueType;
		}

		@Override
		public void change(IObservable observable) {
			((DecoratingObservableValueStub) observable).decorated
					.setValue(new Object());
		}
	}

	static class DecoratingObservableValueStub extends
			DecoratingObservableValue<Object> {
		IObservableValue<Object> decorated;

		DecoratingObservableValueStub(IObservableValue<Object> decorated) {
			super(decorated, true);
			this.decorated = decorated;
		}
	}
}
