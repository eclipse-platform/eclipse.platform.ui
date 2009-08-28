/*******************************************************************************
 * Copyright (c) 2007, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 208858, 221351, 213145
 *     Ovidio Mallo - bug 241318
 *     Tom Schindl - bug 287601
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable.masterdetail;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableList;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.databinding.conformance.MutableObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.DisposeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 */
public class DetailObservableListTest extends AbstractDefaultRealmTestCase {
	/**
	 * Asserts the use case of specifying null on construction for the detail
	 * type of the detail list.
	 * 
	 * @throws Exception
	 */
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
	 * 
	 * @throws Exception
	 */
	public void testElementTypeNotNull() throws Exception {
		WritableValue observableValue = new WritableValue(new WritableList(
				new ArrayList(), Object.class), null);

		WritableListFactory factory = new WritableListFactory();
		DetailObservableList detailObservable = new DetailObservableList(
				factory, observableValue, Object.class);
		assertEquals(factory.type, detailObservable.getElementType());

		try {
			factory.type = String.class;
			observableValue.setValue(new WritableList(Arrays
					.asList(new Object[] { new Object() }), String.class));
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

	public void testDisposeWhileFiringEvents() {
		IObservableValue master = new WritableValue();
		WritableListFactory factory = new WritableListFactory();
		master.setValue("");

		final IObservableList[] detailObservable = new IObservableList[1];

		master.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				detailObservable[0].dispose();
			}
		});

		detailObservable[0] = MasterDetailObservables.detailList(master,
				factory, null);

		master.setValue("New Value");
	}

	private static class WritableListFactory implements IObservableFactory {
		Object type = Object.class;

		public IObservable createObservable(Object target) {
			return new WritableList(new ArrayList(), type);
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(DetailObservableListTest.class
				.getName());
		suite.addTestSuite(DetailObservableListTest.class);
		suite.addTest(MutableObservableListContractTest.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		Object elementType = Object.class;

		public IObservableCollection createObservableCollection(
				final Realm realm, final int elementCount) {

			IObservableValue master = new WritableValue(realm, new Integer(
					elementCount), Integer.class);
			IObservableFactory factory = new FactoryStub(realm, elementType);
			return new DetailObservableListStub(factory, master, elementType);
		}

		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		public Object getElementType(IObservableCollection collection) {
			return elementType;
		}

		public void change(IObservable observable) {
			final IObservableValue master = ((DetailObservableListStub) observable).master;
			master.setValue(new Integer(((Integer) master.getValue())
					.intValue() + 1));
		}
	}

	static class FactoryStub implements IObservableFactory {
		private Realm realm;
		private Object elementType;

		FactoryStub(Realm realm, Object elementType) {
			this.realm = realm;
			this.elementType = elementType;
		}

		Object type = Object.class;

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
