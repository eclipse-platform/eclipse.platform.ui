/*******************************************************************************
 * Copyright (c) 2007-2008 Marko Topolnik and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marko Topolnik - initial API and implementation (bug 184830)
 *     Matthew Hall - bugs 184830, 213145
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.core.internal.databinding.observable.MapEntryObservableValue;
import org.eclipse.jface.databinding.conformance.MutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.ObservableStaleContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestSuite;

/**
 * @since 1.2
 */
public class MapEntryObservableValueTest extends AbstractDefaultRealmTestCase
		implements IValueChangeListener {
	private static final String VALUE1 = "Value1";
	private static final String VALUE2 = "Value2";

	private final Object key = "mapKey";
	private IObservableMap map;
	private ValueDiff diff;
	private MapEntryObservableValue observedValue;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.map = new WritableMap();
		this.observedValue = (MapEntryObservableValue) Observables
				.observeMapEntry(this.map, this.key, String.class);
		observedValue.addValueChangeListener(this);
	}

	@Override
	public void handleValueChange(ValueChangeEvent p_event) {
		this.diff = p_event.diff;
	}

	@Test
	public void testNullValue() {
		// test entry added with value null
		this.map.put(this.key, null);
		assertNull(this.diff);
		assertNull(this.observedValue.getValue());
		// test value changed from null to null
		this.map.put(this.key, null);
		assertNull(this.diff);
		assertNull(this.observedValue.getValue());
		// test null-valued entry removed
		this.map.remove(this.key);
		assertNull(this.diff);
		assertNull(this.observedValue.getValue());
	}

	@Test
	public void testNonNullValue() {
		// test add non-null value
		this.map.put(this.key, VALUE1);
		assertNotNull(this.diff);
		assertNull(this.diff.getOldValue());
		assertSame(VALUE1, this.diff.getNewValue());
		assertSame(VALUE1, this.observedValue.getValue());

		// test change to another non-null value
		this.diff = null;
		this.map.put(this.key, VALUE2);
		assertNotNull(this.diff);
		assertSame(VALUE1, this.diff.getOldValue());
		assertSame(VALUE2, this.diff.getNewValue());
		assertSame(VALUE2, this.observedValue.getValue());
	}

	@Test
	public void testTransitionBetweenNullAndNonNull() {
		this.map.put(this.key, null);

		// test transition to non-null
		this.diff = null;
		this.map.put(this.key, VALUE1);
		assertNotNull(this.diff);
		assertNull(this.diff.getOldValue());
		assertSame(VALUE1, this.diff.getNewValue());

		// test transition to null
		this.diff = null;
		this.map.put(this.key, null);
		assertNotNull(this.diff);
		assertSame(VALUE1, this.diff.getOldValue());
		assertNull(this.diff.getNewValue());
	}

	@Test
	public void testRemoveKey() {
		this.map.put(this.key, VALUE1);

		this.diff = null;
		this.map.remove(this.key);
		assertNotNull(this.diff);
		assertSame(VALUE1, this.diff.getOldValue());
		assertNull(this.diff.getNewValue());
	}

	@Test
	public void testGetAndSetValue() {
		// test set null value
		this.observedValue.setValue(null);
		assertNull(this.observedValue.getValue());
		assertNull(this.diff);

		// test set non-null value
		this.observedValue.setValue(VALUE1);
		assertSame(VALUE1, this.observedValue.getValue());
		assertNotNull(this.diff);
		assertNull(this.diff.getOldValue());
		assertSame(VALUE1, this.diff.getNewValue());

		// test set another non-null value
		this.diff = null;
		this.observedValue.setValue(VALUE2);
		assertSame(VALUE2, this.observedValue.getValue());
		assertNotNull(this.diff);
		assertSame(VALUE1, this.diff.getOldValue());
		assertSame(VALUE2, this.diff.getNewValue());
	}

	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(MutableObservableValueContractTest.suite(new Delegate()));
		suite.addTest(ObservableStaleContractTest.suite(new Delegate()));
	}

	private static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Object valueType = new Object();

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			WritableMap map = new WritableMap(realm);
			Object key = new Object();
			map.put(key, new Object());
			return new MapEntryObservableValueStub(map, key, valueType);
		}

		@Override
		public Object createValue(IObservableValue observable) {
			return new Object();
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return valueType;
		}

		@Override
		public void change(IObservable observable) {
			MapEntryObservableValueStub mapEntryValue = (MapEntryObservableValueStub) observable;
			mapEntryValue.map
					.put(mapEntryValue.key, createValue(mapEntryValue));
		}

		@Override
		public void setStale(IObservable observable, boolean stale) {
			MapEntryObservableValueStub mapEntryValue = (MapEntryObservableValueStub) observable;
			mapEntryValue.map.setStale(stale);
		}
	}

	private static class MapEntryObservableValueStub extends
			MapEntryObservableValue {
		WritableMap map;
		Object key;

		MapEntryObservableValueStub(WritableMap map, Object key,
				Object valueType) {
			super(map, key, valueType);
			this.map = map;
			this.key = key;
		}
	}
}
