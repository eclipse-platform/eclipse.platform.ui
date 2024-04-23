/*******************************************************************************
 * Copyright (c) 2007, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 211786)
 *     Matthew Hall - bug 213145
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.AbstractObservable;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.ComputedList;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.TestCollection;
import org.eclipse.jface.databinding.conformance.util.ListChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

public class ComputedListTest extends AbstractDefaultRealmTestCase {
	ComputedListStub<Object> list;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		list = new ComputedListStub<>();
		list.size(); // Force list to compute
	}

	@Test
	public void testDependency_Staleness() {
		assertFalse(list.isStale());
		list.dependency.fireStale();
		assertTrue(list.isStale());
	}

	@Test
	public void testDependency_FiresListChange() {
		assertEquals(list.nextComputation, list);

		Object element = new Object();
		list.nextComputation.add(element);

		list.dependency.fireChange();

		List<Object> expectedList = new ArrayList<>();
		expectedList.add(element);
		assertEquals(expectedList, list);
	}

	@Test
	public void testDependency_NoStaleEventIfAlreadyDirty() {
		list.dependency.fireChange();
		list.addStaleListener(staleEvent -> fail("Should not fire stale when list is already dirty"));
		list.dependency.fireStale();
	}

	@Test
	public void testDependency_ListChangeEventFiresOnlyWhenNotDirty() {
		ListChangeEventTracker<Object> tracker = ListChangeEventTracker.observe(list);

		list.dependency.fireChange();
		assertEquals(
				"ComputedList should fire list change event when its dependency changes",
				1, tracker.count);

		list.dependency.fireChange();
		assertEquals(
				"ComputedList should not fire list change events when dirty",
				1, tracker.count);

		list.size(); // Force list to recompute.
		list.dependency.fireChange();
		assertEquals(
				"ComputedList should fire list change event when its dependency changes",
				2, tracker.count);
	}

	@Test
	public void testCreate() throws Exception {
		WritableList<Integer> writeList = new WritableList<>();
		writeList.add(44);
		IObservableList<Integer> compList = ComputedList.create(() -> new ArrayList<>(writeList));
		assertEquals(writeList, compList);
		writeList.add(55);
		assertEquals(writeList, compList);
	}

	static class ComputedListStub<E> extends ComputedList<E> {
		List<E> nextComputation = new ArrayList<>();
		ObservableStub dependency;

		ComputedListStub() {
			this(Realm.getDefault());
		}

		ComputedListStub(Realm realm) {
			super(realm);
			dependency = new ObservableStub(realm);
		}

		@Override
		protected List<E> calculate() {
			ObservableTracker.getterCalled(dependency);
			return new ArrayList<>(nextComputation);
		}
	}

	static class ObservableStub extends AbstractObservable {
		public ObservableStub(Realm realm) {
			super(realm);
		}

		boolean stale;

		@Override
		public boolean isStale() {
			return stale;
		}

		@Override
		protected void fireStale() {
			stale = true;
			super.fireStale();
		}

		@Override
		protected void fireChange() {
			super.fireChange();
		}
	}

	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(ObservableListContractTest.class, new Delegate());
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate<Object> {
		@Override
		public IObservableCollection<Object> createObservableCollection(Realm realm,
				int elementCount) {
			final ComputedListStub<Object> list = new ComputedListStub<>(realm);
			for (int i = 0; i < elementCount; i++)
				list.nextComputation.add(createElement(list));
			list.size(); // force list to compute
			return list;
		}

		@Override
		public void change(IObservable observable) {
			@SuppressWarnings("unchecked")
			ComputedListStub<Object> list = (ComputedListStub<Object>) observable;
			list.nextComputation.add(new Object());
			list.dependency.fireChange();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void setStale(IObservable observable, boolean stale) {
			if (stale)
				((ComputedListStub<Object>) observable).dependency.fireStale();
			else {
				ComputedListStub<Object> computedList = (ComputedListStub<Object>) observable;
				computedList.dependency.stale = false;
				computedList.dependency.fireChange();
			}
		}
	}
}
