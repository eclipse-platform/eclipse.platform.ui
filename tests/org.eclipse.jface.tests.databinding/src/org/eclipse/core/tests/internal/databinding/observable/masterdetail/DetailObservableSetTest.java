/*******************************************************************************
 * Copyright (c) 2007 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable.masterdetail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableSet;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.jface.databinding.conformance.MutableObservableSetContractTest;
import org.eclipse.jface.databinding.conformance.ObservableCollectionContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;
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
		WritableValue observableValue = new WritableValue(new WritableSet(new HashSet(), Object.class),
				null);

		class Factory implements IObservableFactory {
			Object type;

			public IObservable createObservable(Object target) {
				return new WritableSet(new HashSet(), type);
			}
		}

		Factory factory = new Factory();
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
		WritableValue observableValue = new WritableValue(new WritableSet(new HashSet(), Object.class),
				null);

		class Factory implements IObservableFactory {
			Object type = Object.class;

			public IObservable createObservable(Object target) {
				return new WritableSet(new HashSet(), type);
			}
		}

		Factory factory = new Factory();
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

	public static Test suite() {
		return new SuiteBuilder().addTests(DetailObservableSetTest.class)
				.addObservableContractTest(
						ObservableCollectionContractTest.class, new Delegate())
				.addObservableContractTest(
						MutableObservableSetContractTest.class, new Delegate())
				.build();
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		Object elementType = Object.class;

		public IObservableCollection createObservableCollection(
				final Realm realm, final int elementCount) {

			IObservableValue master = new WritableValue(realm, new Integer(
					elementCount), Integer.class);
			IObservableFactory factory = new FactoryStub(realm, elementType);
			return new DetailObservableSetStub(factory, master, elementType);
		}

		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		public Object getElementType(IObservableCollection collection) {
			return elementType;
		}

		public void change(IObservable observable) {
			final IObservableValue master = ((DetailObservableSetStub) observable).master;
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
