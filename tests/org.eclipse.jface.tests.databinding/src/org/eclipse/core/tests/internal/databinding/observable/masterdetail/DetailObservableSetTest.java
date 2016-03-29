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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableSet;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.databinding.conformance.MutableObservableSetContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.DisposeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 *
 */
public class DetailObservableSetTest extends AbstractDefaultRealmTestCase {
	/**
	 * Asserts the use case of specifying null on construction for the detail
	 * type of the detail set.
	 *
	 * @throws Exception
	 */
	public void testElementTypeNull() throws Exception {
		WritableValue observableValue = new WritableValue(new WritableSet(
				new HashSet(), Object.class), null);

		WritableSetFactory factory = new WritableSetFactory();
		DetailObservableSet detailObservable = new DetailObservableSet(factory,
				observableValue, null);
		assertNull(detailObservable.getElementType());

		factory.type = Object.class;
		observableValue.setValue(new WritableSet(Arrays
				.asList(new Object[] { new Object() }), String.class));
		assertNull("element type not null", detailObservable.getElementType());

		factory.type = String.class;
		// set the value again to ensure that the observable doesn't update the
		// element type with that of the new element type
		observableValue.setValue(new WritableSet(Arrays
				.asList(new String[] { "1" }), Object.class));
		assertNull("element type not null", detailObservable.getElementType());
	}

	/**
	 * Asserts that you can't change the type across multiple inner observables.
	 *
	 * @throws Exception
	 */
	public void testElementTypeNotNull() throws Exception {
		WritableValue observableValue = new WritableValue(new WritableSet(
				new HashSet(), Object.class), null);

		WritableSetFactory factory = new WritableSetFactory();
		DetailObservableSet detailObservable = new DetailObservableSet(factory,
				observableValue, Object.class);
		assertEquals(factory.type, detailObservable.getElementType());

		try {
			factory.type = String.class;
			observableValue.setValue(new WritableSet(Arrays
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

			@Override
			public synchronized void dispose() {
				disposed = true;
				super.dispose();
			}
		}

		OuterObservable outerObservable = new OuterObservable();
		WritableSetFactory factory = new WritableSetFactory();
		DetailObservableSet detailObservable = new DetailObservableSet(factory,
				outerObservable, null);

		assertFalse(outerObservable.disposed);

		detailObservable.dispose();
		assertFalse(outerObservable.disposed);
	}

	public void testDisposeMasterDisposesDetail() {
		IObservableValue master = new WritableValue();
		WritableSetFactory factory = new WritableSetFactory();
		master.setValue("");

		IObservableSet detailObservable = MasterDetailObservables.detailSet(
				master, factory, null);
		DisposeEventTracker tracker = DisposeEventTracker
				.observe(detailObservable);

		master.dispose();

		assertEquals(1, tracker.count);
		assertTrue(detailObservable.isDisposed());
	}

	public void testDisposeWhileFiringEvents() {
		IObservableValue master = new WritableValue();
		WritableSetFactory factory = new WritableSetFactory();
		master.setValue("");

		final IObservableSet[] detailObservable = new IObservableSet[1];

		master.addValueChangeListener(new IValueChangeListener() {
			@Override
			public void handleValueChange(ValueChangeEvent event) {
				detailObservable[0].dispose();
			}
		});

		detailObservable[0] = MasterDetailObservables.detailSet(master,
				factory, null);

		master.setValue("New Value");
	}

	private static class WritableSetFactory implements IObservableFactory {
		Object type = Object.class;

		@Override
		public IObservable createObservable(Object target) {
			return new WritableSet(new HashSet(), type);
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(DetailObservableSetTest.class.getName());
		suite.addTestSuite(DetailObservableSetTest.class);
		suite.addTest(MutableObservableSetContractTest.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		Object elementType = Object.class;

		@Override
		public IObservableCollection createObservableCollection(
				final Realm realm, final int elementCount) {

			IObservableValue master = new WritableValue(realm, Integer.valueOf(
					elementCount), Integer.class);
			IObservableFactory factory = new FactoryStub(realm, elementType);
			return new DetailObservableSetStub(factory, master, elementType);
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
			final IObservableValue master = ((DetailObservableSetStub) observable).master;
			master.setValue(Integer.valueOf(((Integer) master.getValue())
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

		@Override
		public IObservable createObservable(Object target) {
			int elementCount = ((Integer) target).intValue();
			final Set wrappedSet = new HashSet();
			for (int i = 0; i < elementCount; i++)
				wrappedSet.add(new Object());
			return new WritableSet(realm, wrappedSet, elementType);
		}
	}

	static class DetailObservableSetStub extends DetailObservableSet {
		IObservableValue master;

		DetailObservableSetStub(IObservableFactory factory,
				IObservableValue master, Object elementType) {
			super(factory, master, elementType);
			this.master = master;
		}
	}
}
