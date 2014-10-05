/*******************************************************************************
 * Copyright (c) 2007, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 221351, 213145
 *     Ovidio Mallo - bug 241318
 *     Tom Schindl - bug 287601
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable.masterdetail;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.map.WritableMap;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableMap;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.databinding.conformance.util.DisposeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 * 
 */
public class DetailObservableMapTest extends AbstractDefaultRealmTestCase {
	/**
	 * Asserts the use case of specifying null on construction for the detail
	 * type of the detail set.
	 * 
	 * @throws Exception
	 */
	public void testKeyValueTypeNull() throws Exception {
		WritableValue observableValue = new WritableValue();

		WritableMapFactory factory = new WritableMapFactory();
		DetailObservableMap detailObservable = new DetailObservableMap(factory,
				observableValue, null, null);
		assertNull(detailObservable.getKeyType());
		assertNull(detailObservable.getValueType());

		factory.keyType = Object.class;
		factory.valueType = Object.class;
		observableValue.setValue(new Object());
		assertNull("key type not null", detailObservable.getKeyType());
		assertNull("value type not null", detailObservable.getValueType());

		factory.keyType = String.class;
		factory.valueType = String.class;
		// set the value again to ensure that the observable doesn't update the
		// element type with that of the new element type
		observableValue.setValue(new Object());
		assertNull("key type not null", detailObservable.getKeyType());
		assertNull("value type not null", detailObservable.getValueType());
	}

	/**
	 * Asserts that you can't change the type across multiple inner observables.
	 * 
	 * @throws Exception
	 */
	public void testKeyValueTypeNotNull() throws Exception {
		WritableValue observableValue = new WritableValue();

		WritableMapFactory factory = new WritableMapFactory();
		DetailObservableMap detailObservable = new DetailObservableMap(factory,
				observableValue, Object.class, Object.class);
		assertEquals(Object.class, detailObservable.getKeyType());
		assertEquals(Object.class, detailObservable.getValueType());

		try {
			factory.keyType = String.class;
			factory.valueType = String.class;
			observableValue.setValue(new Object());
			fail("if an element type is set this cannot be changed");
		} catch (AssertionFailedException e) {
		}
	}

	/**
	 * Asserts that the master observable value is not disposed upon disposing
	 * its detail observable value (bug 241318).
	 */
	public void testMasterNotDisposedWhenDetailDisposed() {
		class OuterObservable extends WritableValue {
			boolean disposed = false;

			@Override
			public synchronized void dispose() {
				disposed = true;
				super.dispose();
			}
		}

		OuterObservable outerObservable = new OuterObservable();
		WritableMapFactory factory = new WritableMapFactory();
		DetailObservableMap detailObservable = new DetailObservableMap(factory,
				outerObservable, null, null);

		assertFalse(outerObservable.disposed);

		detailObservable.dispose();
		assertFalse(outerObservable.disposed);
	}

	public void testDisposeMasterDisposesDetail() {
		IObservableValue master = new WritableValue();
		WritableMapFactory factory = new WritableMapFactory();
		master.setValue("");

		IObservableMap detailObservable = MasterDetailObservables.detailMap(
				master, factory, null, null);
		DisposeEventTracker tracker = DisposeEventTracker
				.observe(detailObservable);

		master.dispose();

		assertEquals(1, tracker.count);
		assertTrue(detailObservable.isDisposed());
	}

	public void testDisposeWhileFiringEvents() {
		IObservableValue master = new WritableValue();
		WritableMapFactory factory = new WritableMapFactory();
		master.setValue("");

		final IObservableMap[] detailObservable = new IObservableMap[1];

		master.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				detailObservable[0].dispose();
			}
		});

		detailObservable[0] = MasterDetailObservables.detailMap(master,
				factory, null, null);

		master.setValue("New Value");
	}

	private static class WritableMapFactory implements IObservableFactory {
		Object keyType = Object.class;
		Object valueType = Object.class;

		@Override
		public IObservable createObservable(Object target) {
			return new WritableMap(keyType, valueType);
		}
	}
}
