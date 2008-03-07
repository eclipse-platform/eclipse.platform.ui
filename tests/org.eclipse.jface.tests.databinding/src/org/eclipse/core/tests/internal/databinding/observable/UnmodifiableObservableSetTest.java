/*******************************************************************************
 * Copyright (c) 2006, 2008 Matthew and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *         (through UnmodifiableObservableListTest.java)
 *     Matthew Hall - bugs 208332, 213145
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.IStaleListener;
import org.eclipse.core.databinding.observable.Observables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.StaleEvent;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ISetChangeListener;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.set.SetChangeEvent;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.internal.databinding.observable.UnmodifiableObservableSet;
import org.eclipse.jface.databinding.conformance.ObservableCollectionContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
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
		ChangeCounter mutableListener = new ChangeCounter();
		ChangeCounter unmodifiableListener = new ChangeCounter();

		mutable.addChangeListener(mutableListener);
		unmodifiable.addChangeListener(unmodifiableListener);

		assertEquals(0, mutableListener.count);
		assertEquals(0, unmodifiableListener.count);
		mutable.add("3");
		assertEquals(1, mutableListener.count);
		assertEquals(1, unmodifiableListener.count);
	}

	public void testFiresSetChangeEvents() throws Exception {
		SetChangeCounter mutableListener = new SetChangeCounter();
		SetChangeCounter unmodifiableListener = new SetChangeCounter();

		mutable.addSetChangeListener(mutableListener);
		unmodifiable.addSetChangeListener(unmodifiableListener);

		assertEquals(0, mutableListener.count);
		assertEquals(0, unmodifiableListener.count);

		String element = "3";
		mutable.add(element);
		assertEquals(1, mutableListener.count);
		assertEquals(mutable, mutableListener.source);
		assertEquals(1, mutableListener.diff.getAdditions().size());

		Object addition = mutableListener.diff.getAdditions().toArray()[0];
		assertEquals(element, addition);
		assertEquals(3, mutable.size());

		assertEquals(1, unmodifiableListener.count);
		assertEquals(unmodifiable, unmodifiableListener.source);
		assertEquals(1, unmodifiableListener.diff.getAdditions().size());

		addition = unmodifiableListener.diff.getAdditions().toArray()[0];
		assertEquals(element, addition);
		assertEquals(3, unmodifiable.size());
	}

	public void testFiresStaleEvents() throws Exception {
		StaleCounter mutableListener = new StaleCounter();
		StaleCounter unmodifiableListener = new StaleCounter();

		mutable.addStaleListener(mutableListener);
		unmodifiable.addStaleListener(unmodifiableListener);

		assertEquals(0, mutableListener.count);
		assertEquals(0, unmodifiableListener.count);
		mutable.setStale(true);
		assertEquals(1, mutableListener.count);
		assertEquals(mutable, mutableListener.source);
		assertTrue(mutable.isStale());
		assertEquals(1, unmodifiableListener.count);
		assertEquals(unmodifiable, unmodifiableListener.source);
		assertTrue(unmodifiable.isStale());
	}

	public void testIsStale() throws Exception {
		assertFalse(mutable.isStale());
		assertFalse(unmodifiable.isStale());
		mutable.setStale(true);
		assertTrue(mutable.isStale());
		assertTrue(unmodifiable.isStale());
	}

	public void testSetStaleOnUnmodifiableList() throws Exception {
		try {
			unmodifiable.setStale(true);
			fail("UnsupportedOperationException should have been thrown");
		} catch (UnsupportedOperationException e) {
		}
	}

	private static class StaleCounter implements IStaleListener {
		int count;
		IObservable source;

		public void handleStale(StaleEvent event) {
			count++;
			this.source = event.getObservable();
		}
	}

	private static class ChangeCounter implements IChangeListener {
		int count;
		IObservable source;

		public void handleChange(ChangeEvent event) {
			count++;
			this.source = event.getObservable();
		}
	}

	private static class SetChangeCounter implements ISetChangeListener {
		int count;
		IObservableSet source;
		SetDiff diff;

		public void handleSetChange(SetChangeEvent event) {
			count++;
			this.source = event.getObservableSet();
			this.diff = event.diff;
		}
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
