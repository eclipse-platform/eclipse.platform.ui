/*******************************************************************************
 * Copyright (c) 2007, 2018 Brad Reynolds and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 208858, 221351, 213145
 *     Ovidio Mallo - bug 241318
 *     Tom Schindl - bug 287601
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable.masterdetail;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableList;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.databinding.conformance.MutableObservableListContractTest;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.DisposeEventTracker;
import org.eclipse.jface.databinding.conformance.util.TestCollection;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

/**
 * @since 3.2
 */
public class DetailObservableListTest extends AbstractDefaultRealmTestCase {
	/**
	 * Asserts the use case of specifying null on construction for the detail
	 * type of the detail list.
	 */
	@Test
	public void testElementTypeNull() throws Exception {
		WritableValue observableValue = new WritableValue(new WritableList(
				new ArrayList(), Object.class), null);

		WritableListFactory factory = new WritableListFactory();
		DetailObservableList detailObservable = new DetailObservableList(
				factory, observableValue, null);
		assertNull(detailObservable.getElementType());

		// change the type returned from the factory
		factory.type = String.class;
		observableValue
				.setValue(new WritableList(new ArrayList(), String.class));
		assertNull("element type not null", detailObservable.getElementType());
	}

	/**
	 * Asserts that you can't change the type across multiple inner observables.
	 */
	@Test
	public void testElementTypeNotNull() throws Exception {
		WritableValue observableValue = new WritableValue(new WritableList(
				new ArrayList(), Object.class), null);

		WritableListFactory factory = new WritableListFactory();
		DetailObservableList detailObservable = new DetailObservableList(
				factory, observableValue, Object.class);
		assertEquals(factory.type, detailObservable.getElementType());

		factory.type = String.class;
		assertThrows("if an element type is set this cannot be changed", AssertionFailedException.class,
				() -> observableValue
						.setValue(new WritableList(List.of(new Object()), String.class)));
	}

	/**
	 * Asserts that the master observable value is not disposed upon disposing
	 * its detail observable value (bug 241318).
	 */
	@Test
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
		WritableListFactory factory = new WritableListFactory();
		DetailObservableList detailObservable = new DetailObservableList(
				factory, outerObservable, null);

		assertFalse(outerObservable.disposed);

		detailObservable.dispose();
		assertFalse(outerObservable.disposed);
	}

	@Test
	public void testDisposeMasterDisposesDetail() {
		IObservableValue master = new WritableValue();
		WritableListFactory factory = new WritableListFactory();
		master.setValue("");

		IObservableList detailObservable = MasterDetailObservables.detailList(
				master, factory, null);
		DisposeEventTracker tracker = DisposeEventTracker
				.observe(detailObservable);

		master.dispose();

		assertEquals(1, tracker.count);
		assertTrue(detailObservable.isDisposed());
	}

	@Test
	public void testDisposeWhileFiringEvents() {
		IObservableValue master = new WritableValue();
		WritableListFactory factory = new WritableListFactory();
		master.setValue("");

		final IObservableList[] detailObservable = new IObservableList[1];

		master.addValueChangeListener(event -> detailObservable[0].dispose());

		detailObservable[0] = MasterDetailObservables.detailList(master,
				factory, null);

		master.setValue("New Value");
	}

	private static class WritableListFactory implements IObservableFactory {
		Object type = Object.class;

		@Override
		public IObservable createObservable(Object target) {
			return new WritableList(new ArrayList(), type);
		}
	}

	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(MutableObservableListContractTest.class, new Delegate());
		suite.addTest(ObservableListContractTest.class, new Delegate());

	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		Object elementType = Object.class;

		@Override
		public IObservableCollection createObservableCollection(
				final Realm realm, final int elementCount) {

			IObservableValue master = new WritableValue(realm, Integer.valueOf(
					elementCount), Integer.class);
			IObservableFactory factory = new FactoryStub(realm, elementType);
			return new DetailObservableListStub(factory, master, elementType);
		}

		@Override
		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		@Override
		public Object getElementType(IObservableCollection collection) {
			return elementType;
		}

		@Override
		public void change(IObservable observable) {
			final IObservableValue master = ((DetailObservableListStub) observable).master;
			master.setValue(Integer.valueOf(((Integer) master.getValue())
					.intValue() + 1));
		}
	}

	static class FactoryStub implements IObservableFactory {
		private final Realm realm;
		private final Object elementType;

		FactoryStub(Realm realm, Object elementType) {
			this.realm = realm;
			this.elementType = elementType;
		}

		Object type = Object.class;

		@Override
		public IObservable createObservable(Object target) {
			int elementCount = ((Integer) target).intValue();
			final ArrayList wrappedList = new ArrayList();
			for (int i = 0; i < elementCount; i++)
				wrappedList.add(new Object());
			return new WritableList(realm, wrappedList, elementType);
		}
	}

	static class DetailObservableListStub extends DetailObservableList {
		IObservableValue master;

		DetailObservableListStub(IObservableFactory factory,
				IObservableValue master, Object elementType) {
			super(factory, master, elementType);
			this.master = master;
		}
	}
}
