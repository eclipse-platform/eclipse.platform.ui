/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 219909)
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableValue;
import org.eclipse.jface.databinding.conformance.ObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;

import junit.framework.Test;

/**
 * @since 3.2
 * 
 */
public class UnmodifiableObservableValueTest {
	public static Test suite() {
		return new SuiteBuilder().addObservableContractTest(
				ObservableValueContractTest.class, new Delegate()).build();
	}

	private static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Object valueType = new Object();

		public IObservableValue createObservableValue(Realm realm) {
			return new UnmodifiableObservableValueStub(new WritableValue(realm,
					null, valueType));
		}

		public Object getValueType(IObservableValue observable) {
			return valueType;
		}

		public Object createValue(IObservableValue observable) {
			return new Object();
		}

		public void change(IObservable observable) {
			UnmodifiableObservableValueStub wrapper = (UnmodifiableObservableValueStub) observable;
			wrapper.wrappedValue.setValue(createValue(wrapper));
		}
	}

	private static class UnmodifiableObservableValueStub extends
			UnmodifiableObservableValue {
		IObservableValue wrappedValue;

		UnmodifiableObservableValueStub(IObservableValue wrappedValue) {
			super(wrappedValue);
			this.wrappedValue = wrappedValue;
		}
	}
}
