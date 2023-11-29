/*******************************************************************************
 * Copyright (c) 2006, 2008 Brad Reynolds
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - bug 158687
 *     Brad Reynolds - bug 164653
 *     Matthew Hall - bug 213145
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.conformance.MutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.TestCollection;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;

/**
 * @since 3.2
 */
public class WritableValueTest extends AbstractDefaultRealmTestCase {
	/**
	 * All constructors delegate to the 3 arg constructor.
	 */
	@Test
	public void testConstructor() throws Exception {
		WritableValue<Object> value = new WritableValue<>(DisplayRealm.getRealm(Display
				.getDefault()));
		assertNull(value.getValue());
		assertNull(value.getValueType());
	}

	@Test
	public void testWithValueType() throws Exception {
		Object elementType = String.class;
		WritableValue<String> value = WritableValue.withValueType(elementType);
		assertNotNull(value);
		assertEquals(Realm.getDefault(), value.getRealm());
		assertEquals(elementType, value.getValueType());
	}

	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(MutableObservableValueContractTest.class, new Delegate());
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		@Override
		public IObservableValue<?> createObservableValue(Realm realm) {
			return new WritableValue<>(realm, "", String.class);
		}

		@Override
		public void change(IObservable observable) {
			@SuppressWarnings("unchecked")
			IObservableValue<String> observableValue = (IObservableValue<String>) observable;
			observableValue.setValue(createValue(observableValue));
		}

		@Override
		public Object getValueType(IObservableValue<?> observable) {
			return String.class;
		}

		@Override
		public String createValue(IObservableValue<?> observable) {
			return observable.getValue() + "a";
		}
	}
}
