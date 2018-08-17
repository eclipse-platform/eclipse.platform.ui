/*******************************************************************************
 * Copyright (c) 2006, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653, 147515
 *     Matthew Hall - bug 213145
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable.list;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.conformance.MutableObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestSuite;

/**
 * @since 3.2
 */
public class WritableListTest {

	@Before
	public void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
	}

	@After
	public void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}

	@Test
	public void testSetRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableList list = new WritableList();
			list.add("");
			list.set(0, "");
		});
	}

	@Test
	public void testAddRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableList list = new WritableList();
			list.add("");
		});
	}

	@Test
	public void testAddByIndexRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableList list = new WritableList();
			list.add(0, "");
		});
	}

	@Test
	public void testAddAllRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableList list = new WritableList();
			list.addAll(Collections.EMPTY_LIST);
		});
	}

	@Test
	public void testAddAllByIndexRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableList list = new WritableList();
			list.addAll(0, Collections.EMPTY_LIST);
		});
	}

	@Test
	public void testRemoveRealmChecks() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		final WritableList list = new WritableList();
		list.add("");
		list.add("");

		RealmTester.exerciseCurrent(() -> list.remove(""));
		RealmTester.setDefault(null);
	}

	@Test
	public void testRemoveByIndexRealmChecks() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		final WritableList list = new WritableList();
		list.add("");
		list.add("");

		RealmTester.exerciseCurrent(() -> list.remove(list.size() - 1));

		RealmTester.setDefault(null);
	}

	@Test
	public void testRemoveAllRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableList list = new WritableList();
			list.removeAll(Collections.EMPTY_LIST);
		});
	}

	@Test
	public void testRetainAllRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableList list = new WritableList();
			list.retainAll(Collections.EMPTY_LIST);
		});
	}

	@Test
	public void testClearRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			WritableList list = new WritableList();
			list.clear();
		});
	}

	@Test
	public void testNullElementType() throws Exception {
		RealmTester.setDefault(DisplayRealm.getRealm(Display.getDefault()));
		WritableList writableList = new WritableList();
		assertNull(writableList.getElementType());

		writableList = new WritableList(Realm.getDefault());
		assertNull(writableList.getElementType());
	}

	@Test
	public void testWithElementType() throws Exception {
		RealmTester.setDefault(DisplayRealm.getRealm(Display.getDefault()));

		Object elementType = String.class;
		WritableList list = WritableList.withElementType(elementType);
		assertNotNull(list);
		assertEquals(Realm.getDefault(), list.getRealm());
		assertEquals(elementType, list.getElementType());
	}

	@Test
	public void testListConstructorsDoNotCopy_1() {
		RealmTester.setDefault(new CurrentRealm(true));
		List list = new ArrayList(Arrays.asList(new Object[] { "a", "b", "c" }));
		WritableList wlist = new WritableList(list, Object.class);
		wlist.remove(1);
		assertEquals(2, list.size());
		list.add("d");
		assertEquals(3, wlist.size());
	}

	@Test
	public void testListConstructorsDoNotCopy_2() {
		List list = new ArrayList(Arrays.asList(new Object[] { "a", "b", "c" }));
		WritableList wlist = new WritableList(new CurrentRealm(true), list,
				Object.class);
		wlist.remove(1);
		assertEquals(2, list.size());
		list.add("d");
		assertEquals(3, wlist.size());
	}

	@Test
	public void testCollectionConstructorsCopy_1() {
		RealmTester.setDefault(new CurrentRealm(true));
		List list = new ArrayList(Arrays.asList(new Object[] { "a", "b", "c" }));
		WritableList wlist = new WritableList((Collection) list, Object.class);
		wlist.remove(1);
		assertEquals(3, list.size());
		list.add("d");
		assertEquals(2, wlist.size());
	}

	@Test
	public void testCollectionConstructorsCopy_2() {
		List list = new ArrayList(Arrays.asList(new Object[] { "a", "b", "c" }));
		WritableList wlist = new WritableList(new CurrentRealm(true),
				(Collection) list, Object.class);
		wlist.remove(1);
		assertEquals(3, list.size());
		list.add("d");
		assertEquals(2, wlist.size());
	}

	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(MutableObservableListContractTest.suite(new Delegate()));
	}

	/* package */static class Delegate extends
			AbstractObservableCollectionContractDelegate {
		@Override
		public Object createElement(IObservableCollection collection) {
			return String.valueOf(collection.size() + 1);
		}

		@Override
		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}

		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				final int itemCount) {
			WritableList observable = new WritableList(realm, new ArrayList(),
					String.class);

			for (int i = 0; i < itemCount; i++) {
				observable.add(String.valueOf(i));
			}

			return observable;
		}

		@Override
		public void change(IObservable observable) {
			((WritableList) observable).add("");
		}
	}
}
