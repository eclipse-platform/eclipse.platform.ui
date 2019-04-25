/*******************************************************************************
 * Copyright (c) 2006, 2018 Brad Reynolds and others.
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
 *     Brad Reynolds - bug 167204
 *     Matthew Hall - bugs 208858, 213145, 247367, 349038
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.list;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.AbstractObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestSuite;

/**
 * @since 3.2
 */
public class AbstractObservableListTest {
	private AbstractObservableListStub<Object> list;

	@Before
	public void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		list = new AbstractObservableListStub<>();
	}

	@After
	public void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}

	@Test
	public void testFireChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> list.fireChange());
	}

	@Test
	public void testFireStaleRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> list.fireStale());
	}

	@Test
	public void testFireListChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> list.fireListChange(null));
	}

	@Test
	public void testMove_FiresListChanges() throws Exception {
		list = new MutableObservableListStub<>();
		final Object element = new Object();
		list.add(element);
		list.add(new Object());

		final List<ListDiffEntry<?>> diffEntries = new ArrayList<>();
		list.addListChangeListener(event -> diffEntries.addAll(Arrays.asList(event.diff.getDifferences())));

		list.move(0, 1);

		assertEquals(2, diffEntries.size());

		ListDiffEntry<?> entry = diffEntries.get(0);
		assertEquals(element, entry.getElement());
		assertEquals(false, entry.isAddition());
		assertEquals(0, entry.getPosition());

		entry = diffEntries.get(1);
		assertEquals(element, entry.getElement());
		assertEquals(true, entry.isAddition());
		assertEquals(1, entry.getPosition());
	}

	@Test
	public void testMove_MovesElement() throws Exception {
		list = new MutableObservableListStub<>();
		final Object element0 = new Object();
		final Object element1 = new Object();
		list.add(element0);
		list.add(element1);

		list.move(0, 1);

		assertEquals(element1, list.get(0));
		assertEquals(element0, list.get(1));
	}

	@Test
	public void testAddListChangeListener_AfterDispose() {
		list.dispose();
		list.addListChangeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testRemoveListChangeListener_AfterDispose() {
		list.dispose();
		list.removeListChangeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testAddChangeListener_AfterDispose() {
		list.dispose();
		list.addChangeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testRemoveChangeListener_AfterDispose() {
		list.dispose();
		list.removeChangeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testAddStaleListener_AfterDispose() {
		list.dispose();
		list.addStaleListener(staleEvent -> {
			// do nothing
		});
	}

	@Test
	public void testRemoveStaleListener_AfterDispose() {
		list.dispose();
		list.removeStaleListener(staleEvent -> {
			// do nothing
		});
	}

	@Test
	public void testAddDisposeListener_AfterDispose() {
		list.dispose();
		list.addDisposeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testRemoveDisposeListener_AfterDispose() {
		list.dispose();
		list.removeDisposeListener(event -> {
			// do nothing
		});
	}

	@Test
	public void testHasListeners_AfterDispose() {
		list.dispose();
		list.hasListeners();
	}

	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(ObservableListContractTest.suite(new Delegate()));
	}

	/* package */static class Delegate extends AbstractObservableCollectionContractDelegate<String> {

		@Override
		public IObservableCollection<String> createObservableCollection(Realm realm, final int itemCount) {

			String[] items = new String[itemCount];
			for (int i = 0; i < itemCount; i++) {
				items[i] = String.valueOf(i);
			}

			AbstractObservableListStub<String> observable = new AbstractObservableListStub<>(realm,
					Arrays.asList(items));
			observable.elementType = String.class;
			return observable;
		}

		@Override
		public Object getElementType(IObservableCollection<String> collection) {
			return String.class;
		}

		@Override
		public void change(IObservable observable) {
			((AbstractObservableListStub<?>) observable).fireChange();
		}
	}

	static class AbstractObservableListStub<E> extends AbstractObservableList<E> {
		Object elementType;

		List<E> wrappedList;

		public AbstractObservableListStub() {
			super();
			wrappedList = new ArrayList<>();
		}

		public AbstractObservableListStub(Realm realm, List<E> list) {
			super(realm);
			this.wrappedList = list;
		}

		@Override
		protected int doGetSize() {
			return wrappedList.size();
		}

		@Override
		public E get(int index) {
			ObservableTracker.getterCalled(this);
			return wrappedList.get(index);
		}

		@Override
		public Object getElementType() {
			return elementType;
		}

		@Override
		protected void fireChange() {
			super.fireChange();
		}

		@Override
		protected void fireStale() {
			super.fireStale();
		}

		@Override
		protected void fireListChange(ListDiff<E> diff) {
			super.fireListChange(diff);
		}

		@Override
		protected synchronized boolean hasListeners() {
			return super.hasListeners();
		}
	}

	static class MutableObservableListStub<E> extends AbstractObservableListStub<E> {
		// These methods are present so we can test
		// AbstractObservableList.move()

		@Override
		public void add(int index, E element) {
			checkRealm();
			wrappedList.add(index, element);
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(index, true, element)));
		}

		@Override
		public E remove(int index) {
			checkRealm();
			E element = wrappedList.remove(index);
			fireListChange(Diffs.createListDiff(Diffs.createListDiffEntry(index, false, element)));
			return element;
		}
	}
}
