/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 237718)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.value;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.DecoratingObservableValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.conformance.MutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;

/**
 * @since 3.2
 * 
 */
public class DecoratingObservableValueTest {
	public static Test suite() {
		TestSuite suite = new TestSuite(DecoratingObservableValueTest.class
				.getName());
		suite.addTest(MutableObservableValueContractTest.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableValueContractDelegate {
		private Object valueType = Object.class;

		public IObservableValue createObservableValue(Realm realm) {
			IObservableValue decorated = new WritableValue(realm, new Object(),
					valueType);
			return new DecoratingObservableValueStub(decorated);
		}

		public Object getValueType(IObservableValue observable) {
			return valueType;
		}

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
