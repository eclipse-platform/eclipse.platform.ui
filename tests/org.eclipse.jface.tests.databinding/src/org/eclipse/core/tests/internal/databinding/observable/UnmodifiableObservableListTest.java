/*******************************************************************************
 * Copyright (c) 2006, 2009 Cerner Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 208332, 213145, 237718
 *     Ovidio Mallo - bug 247741
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableList;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.ListChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.StaleEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class UnmodifiableObservableListTest extends
		AbstractDefaultRealmTestCase {
	IObservableList unmodifiable;
	ObservableList mutable;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		List list = new ArrayList();
		list.add("1");
		list.add("2");

		mutable = new MutableObservableList(list, String.class);
		unmodifiable = Observables.unmodifiableObservableList(mutable);
	}

	public void testFiresChangeEvents() throws Exception {
		ChangeEventTracker mutableListener = new ChangeEventTracker();
		ChangeEventTracker unmodifiableListener = new ChangeEventTracker();

		mutable.addChangeListener(mutableListener);
		unmodifiable.addChangeListener(unmodifiableListener);

		assertEquals(0, mutableListener.count);
		assertEquals(0, unmodifiableListener.count);
		mutable.add("3");
		assertEquals(1, mutableListener.count);
		assertEquals(1, unmodifiableListener.count);
	}

	public void testFiresListChangeEvents() throws Exception {
		ListChangeEventTracker mutableListener = new ListChangeEventTracker();
		ListChangeEventTracker unmodifiableListener = new ListChangeEventTracker();

		mutable.addListChangeListener(mutableListener);
		unmodifiable.addListChangeListener(unmodifiableListener);

		assertEquals(0, mutableListener.count);
		assertEquals(0, unmodifiableListener.count);

		String element = "3";
		mutable.add(element);
		assertEquals(1, mutableListener.count);
		assertEquals(mutable, mutableListener.event.getObservableList());
		assertEquals(1, mutableListener.event.diff.getDifferences().length);

		ListDiffEntry difference = mutableListener.event.diff.getDifferences()[0];
		assertEquals(element, difference.getElement());
		assertTrue(difference.isAddition());
		assertEquals(3, mutable.size());

		assertEquals(1, unmodifiableListener.count);
		assertEquals(unmodifiable, unmodifiableListener.event.getObservableList());
		assertEquals(1, unmodifiableListener.event.diff.getDifferences().length);

		difference = unmodifiableListener.event.diff.getDifferences()[0];
		assertEquals(element, difference.getElement());
		assertTrue(difference.isAddition());
		assertEquals(3, unmodifiable.size());
	}

	public void testFiresStaleEvents() throws Exception {
		StaleEventTracker mutableListener = new StaleEventTracker();
		StaleEventTracker unmodifiableListener = new StaleEventTracker();

		mutable.addStaleListener(mutableListener);
		unmodifiable.addStaleListener(unmodifiableListener);

		assertEquals(0, mutableListener.count);
		assertEquals(0, unmodifiableListener.count);
		mutable.setStale(true);
		assertEquals(1, mutableListener.count);
		assertEquals(mutable, mutableListener.event.getObservable());
		assertTrue(mutable.isStale());
		assertEquals(1, unmodifiableListener.count);
		assertEquals(unmodifiable, unmodifiableListener.event.getObservable());
		assertTrue(unmodifiable.isStale());
	}

	public void testIsStale() throws Exception {
		assertFalse(mutable.isStale());
		assertFalse(unmodifiable.isStale());
		mutable.setStale(true);
		assertTrue(mutable.isStale());
		assertTrue(unmodifiable.isStale());
	}

	private static class MutableObservableList extends ObservableList {
		/**
		 * @param wrappedList
		 * @param elementType
		 */
		public MutableObservableList(List wrappedList, Object elementType) {
			super(wrappedList, elementType);
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.ObservableList#add(java.lang.Object)
		 */
		@Override
		public boolean add(Object o) {
			boolean result = wrappedList.add(o);
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(
					wrappedList.size() - 1, true, o)));

			return result;
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(UnmodifiableObservableListTest.class.getName());
		suite.addTestSuite(UnmodifiableObservableListTest.class);
		suite.addTest(ObservableListContractTest.suite(new Delegate()));
		return suite;
	}

	private static class Delegate extends
			AbstractObservableCollectionContractDelegate {
		private Object elementType = new Object();

		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			IObservableList backingList = new WritableList(realm,
					new ArrayList(), elementType);
			IObservableList result = new UnmodifiableObservableListStub(
					backingList);
			for (int i = 0; i < elementCount; i++)
				backingList.add(createElement(result));
			return result;
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
			UnmodifiableObservableListStub unmodifiableList = (UnmodifiableObservableListStub) observable;
			IObservableList wrappedList = unmodifiableList.wrappedList;
			wrappedList.add(createElement(unmodifiableList));
		}
	}

	private static class UnmodifiableObservableListStub extends
			UnmodifiableObservableList {
		IObservableList wrappedList;

		UnmodifiableObservableListStub(IObservableList wrappedList) {
			super(wrappedList);
			this.wrappedList = wrappedList;
		}
	}
}
