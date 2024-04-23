/*******************************************************************************
 * Copyright (c) 2008, 2018 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 222289)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.core.databinding.observable.list.MultiList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.TestCollection;
import org.eclipse.jface.databinding.conformance.util.ListChangeEventTracker;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Before;
import org.junit.Test;

public class MultiListTest extends AbstractDefaultRealmTestCase {
	MultiListStub multiList;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		@SuppressWarnings("unchecked")
		WritableList<Object>[] lists = new WritableList[] { new WritableList<>(), new WritableList<>() };
		multiList = new MultiListStub(Realm.getDefault(), lists);
	}

	@Test
	public void testIsStale_FollowsSublist() {
		assertFalse(multiList.isStale());
		multiList.subLists[0].setStale(true);
		assertTrue(multiList.isStale());
		multiList.subLists[0].setStale(false);
		assertFalse(multiList.isStale());
	}

	@Test
	public void testDependency_FiresListChange() {
		List<Object> expectedList = new ArrayList<>();
		assertEquals(expectedList, multiList);

		Object element = new Object();
		expectedList.add(element);
		multiList.subLists[0].add(element);
		assertEquals(expectedList, multiList);
	}

	@Test
	public void testStaleEvent_NoFireEventIfAlreadyStale() {
		multiList.subLists[0].setStale(true);
		multiList.addStaleListener(staleEvent -> fail("Should not fire stale when list is already dirty"));
		multiList.subLists[1].setStale(true);
	}

	@Test
	public void testModifySubList_FiresListChangeEventFromMultiList() {
		ListChangeEventTracker<Object> tracker = ListChangeEventTracker.observe(multiList);
		ListDiffEntry<?>[] differences;

		//

		Object element0 = new Object();
		multiList.subLists[0].add(element0);

		assertEquals(1, tracker.count);

		differences = tracker.event.diff.getDifferences();
		assertEquals(1, differences.length);
		assertEntry(differences[0], 0, true, element0);

		//

		Object element1 = new Object();
		multiList.subLists[1].add(element1);

		assertEquals(2, tracker.count);

		differences = tracker.event.diff.getDifferences();
		assertEquals(1, differences.length);
		assertEntry(differences[0], 1, true, element1);

		//

		Object element2 = new Object();
		multiList.subLists[0].add(element2);

		assertEquals(3, tracker.count);

		differences = tracker.event.diff.getDifferences();
		assertEquals(1, differences.length);
		assertEntry(differences[0], 1, true, element2);

	}

	private void assertEntry(ListDiffEntry<?> entry, int position,
			boolean addition, Object element) {
		assertEquals(element, entry.getElement());
		assertEquals(addition, entry.isAddition());
		assertEquals(position, entry.getPosition());
	}

	private static class MultiListStub extends MultiList<Object> {
		WritableList<Object>[] subLists;

		MultiListStub(Realm realm, WritableList<Object>[] lists) {
			super(realm, lists);
			this.subLists = lists;
		}
	}

	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(ObservableListContractTest.class, new Delegate());
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate<Object> {
		@Override
		public IObservableCollection<Object> createObservableCollection(Realm realm,
				int elementCount) {
			@SuppressWarnings("unchecked")
			WritableList<Object>[] subLists = new WritableList[] { new WritableList<>(realm),
					new WritableList<>(realm) };
			final MultiListStub list = new MultiListStub(realm, subLists);
			for (int i = 0; i < elementCount; i++)
				list.subLists[0].add(createElement(list));
			return list;
		}

		@Override
		public void change(IObservable observable) {
			MultiListStub list = (MultiListStub) observable;
			list.subLists[0].add(new Object());
		}

		@Override
		public void setStale(IObservable observable, boolean stale) {
			((MultiListStub) observable).subLists[0].setStale(stale);
		}
	}
}
