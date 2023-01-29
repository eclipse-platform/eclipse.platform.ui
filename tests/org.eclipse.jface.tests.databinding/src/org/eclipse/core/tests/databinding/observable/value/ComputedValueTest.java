/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Brad Reynolds - bug 164653
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

/**
 * @since 1.0
 *
 */
public class ComputedValueTest extends AbstractDefaultRealmTestCase {
	@Test
	public void testValueType() throws Exception {
		ComputedValue<Integer> cv = new ComputedValue<Integer>(Integer.TYPE) {
			@Override
			protected Integer calculate() {
				return Integer.valueOf(42);
			}
		};
		assertEquals("value type should be the type that was set", Integer.TYPE, cv.getValueType());

		cv = new ComputedValue<Integer>() {
			@Override
			protected Integer calculate() {
				// TODO Auto-generated method stub
				return null;
			}
		};

		assertNull(cv.getValueType());
	}

	@Test
	public void test_getValue() throws Exception {
		ComputedValue<Integer> cv = new ComputedValue<Integer>() {
			@Override
			protected Integer calculate() {
				return Integer.valueOf(42);
			}
		};
		assertEquals("Calculated value should be 42", Integer.valueOf(42), cv.getValue());
	}

	@Test
	public void testDependencyValueChange() throws Exception {
		final WritableValue<Integer> value = new WritableValue<>(Integer.valueOf(42), Integer.TYPE);

		ComputedValue<Integer> cv = new ComputedValue<Integer>() {
			@Override
			protected Integer calculate() {
				return value.getValue();
			}
		};

		assertEquals("calculated value should have been that of the writable value", value.getValue(), cv.getValue());

		value.setValue(Integer.valueOf(44));

		assertEquals("calculated value should have been that of the writable value", value.getValue(), cv.getValue());
	}

	@Test
	public void testCreate() throws Exception {
		WritableValue<Integer> value = new WritableValue<>(42, null);
		IObservableValue<Integer> cv = ComputedValue.create(value::getValue);
		assertEquals(value.getValue(), cv.getValue());
		value.setValue(44);
		assertEquals(value.getValue(), cv.getValue());
	}

	private static class WritableValueExt<E> extends WritableValue<E> {
		public WritableValueExt(Object valueType, E initialValue) {
			super(initialValue, valueType);
		}

		@Override
		public boolean hasListeners() {
			return super.hasListeners();
		}
	}

	@Test
	public void testHookAndUnhookDependantObservables() throws Exception {
		final List<WritableValue<Integer>> values = new ArrayList<>();

		ComputedValue<Integer> cv = new ComputedValue<Integer>() {
			@Override
			protected Integer calculate() {
				int sum = 0;
				for (Iterator<WritableValue<Integer>> it = values.iterator(); it.hasNext();) {
					WritableValue<Integer> value = it.next();
					sum += value.getValue().intValue();

				}

				return Integer.valueOf(sum);
			}
		};

		WritableValueExt<Integer> value1 = new WritableValueExt<>(Integer.TYPE, Integer.valueOf(1));
		WritableValueExt<Integer> value2 = new WritableValueExt<>(Integer.TYPE, Integer.valueOf(1));
		values.add(value1);
		values.add(value2);

		assertFalse(value1.hasListeners());
		assertFalse(value2.hasListeners());
		cv.getValue();
		assertTrue(value1.hasListeners());
		assertTrue(value2.hasListeners());

		//force the computed value to be stale
		value2.setValue(Integer.valueOf(2));
		//remove value2 from the values that are used to compute the value
		values.remove(value2);

		//force the value to be computed
		cv.getValue();
		assertEquals(Integer.valueOf(1), cv.getValue());
		assertTrue(value1.hasListeners());
		assertFalse("because value2 is not a part of the calculation the listeners should have been removed", value2.hasListeners());
	}

	@Test
	public void testSetValueUnsupportedOperationException() throws Exception {
		ComputedValue<Object> cv = new ComputedValue<Object>() {
			@Override
			protected Object calculate() {
				return null;
			}
		};

		assertThrows(UnsupportedOperationException.class, () -> cv.setValue(new Object()));
	}
}
