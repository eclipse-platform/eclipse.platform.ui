/*******************************************************************************
 * Copyright (c) 2006, 2009 Matthew and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *         (through UnmodifiableObservableListTest.java)
 *     Matthew Hall - bugs 208332, 213145, 237718
 *     Ovidio Mallo - bug 247741
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableSet;
import org.eclipse.jface.databinding.conformance.ObservableCollectionContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.SetChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.StaleEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

public class UnmodifiableObservableSetTest extends AbstractDefaultRealmTestCase {
	UnmodifiableObservableSet unmodifiable;
	MutableObservableSet mutable;

	protected void setUp() throws Exception {
		super.setUp();

		Set set = new HashSet();
		set.add("1");
		set.add("2");

		mutable = new MutableObservableSet(set, String.class);
		unmodifiable = (UnmodifiableObservableSet) Observables
				.unmodifiableObservableSet(mutable);
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

	public void testFiresSetChangeEvents() throws Exception {
		SetChangeEventTracker mutableListener = new SetChangeEventTracker();
		SetChangeEventTracker unmodifiableListener = new SetChangeEventTracker();

		mutable.addSetChangeListener(mutableListener);
		unmodifiable.addSetChangeListener(unmodifiableListener);

		assertEquals(0, mutableListener.count);
		assertEquals(0, unmodifiableListener.count);

		String element = "3";
		mutable.add(element);
		assertEquals(1, mutableListener.count);
		assertEquals(mutable, mutableListener.event.getObservableSet());
		assertEquals(1, mutableListener.event.diff.getAdditions().size());

		Object addition = mutableListener.event.diff.getAdditions().toArray()[0];
		assertEquals(element, addition);
		assertEquals(3, mutable.size());

		assertEquals(1, unmodifiableListener.count);
		assertEquals(unmodifiable, unmodifiableListener.event.getObservableSet());
		assertEquals(1, unmodifiableListener.event.diff.getAdditions().size());

		addition = unmodifiableListener.event.diff.getAdditions().toArray()[0];
		assertEquals(element, addition);
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

	private static class MutableObservableSet extends ObservableSet {
		/**
		 * @param wrappedList
		 * @param elementType
		 */
		public MutableObservableSet(Set wrappedSet, Object elementType) {
			super(wrappedSet, elementType);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.internal.databinding.provisional.observable.list.ObservableList#add(java.lang.Object)
		 */
		public boolean add(Object o) {
			boolean result = wrappedSet.add(o);
			if (result)
				fireSetChange(Diffs.createSetDiff(Collections.singleton(o),
						Collections.EMPTY_SET));
			return result;
		}
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(UnmodifiableObservableSetTest.class.getName());
		suite.addTestSuite(UnmodifiableObservableSetTest.class);
		suite.addTest(ObservableCollectionContractTest.suite(new Delegate()));
		return suite;
	}

	private static class Delegate extends
			AbstractObservableCollectionContractDelegate {
		private Object elementType = new Object();

		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			IObservableSet backingList = new WritableSet(realm, new HashSet(),
					elementType);
			IObservableSet result = new UnmodifiableObservableSetStub(
					backingList);
			for (int i = 0; i < elementCount; i++)
				backingList.add(createElement(result));
			return result;
		}

		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		public Object getElementType(IObservableCollection collection) {
			return elementType;
		}

		public void change(IObservable observable) {
			UnmodifiableObservableSetStub unmodifiableList = (UnmodifiableObservableSetStub) observable;
			IObservableSet wrappedList = unmodifiableList.wrappedSet;
			wrappedList.add(createElement(unmodifiableList));
		}
	}

	private static class UnmodifiableObservableSetStub extends
			UnmodifiableObservableSet {
		IObservableSet wrappedSet;

		UnmodifiableObservableSetStub(IObservableSet wrappedSet) {
			super(wrappedSet);
			this.wrappedSet = wrappedSet;
		}
	}
}
