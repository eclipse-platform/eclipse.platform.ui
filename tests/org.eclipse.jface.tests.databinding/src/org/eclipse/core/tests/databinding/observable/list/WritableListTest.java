/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 164653, 147515
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable.list;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.conformance.databinding.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.conformance.databinding.MutableObservableListContractTest;
import org.eclipse.jface.conformance.databinding.ObservableListContractTest;
import org.eclipse.jface.conformance.databinding.SuiteBuilder;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.RealmTester;
import org.eclipse.jface.tests.databinding.RealmTester.CurrentRealm;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 */
public class WritableListTest extends TestCase {
	protected void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}

	public void testSetRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.add("");
				list.set(0, "");
			}
		});
	}

	public void testAddRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.add("");
			}
		});
	}

	public void testAddByIndexRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.add(0, "");
			}
		});
	}

	public void testAddAllRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.addAll(Collections.EMPTY_LIST);
			}
		});
	}

	public void testAddAllByIndexRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.addAll(0, Collections.EMPTY_LIST);
			}
		});
	}

	public void testRemoveRealmChecks() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		final WritableList list = new WritableList();
		list.add("");
		list.add("");

		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				list.remove("");
			}
		});
		RealmTester.setDefault(null);
	}

	public void testRemoveByIndexRealmChecks() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
		final WritableList list = new WritableList();
		list.add("");
		list.add("");

		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				list.remove(list.size() - 1);
			}
		});

		RealmTester.setDefault(null);
	}

	public void testRemoveAllRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.removeAll(Collections.EMPTY_LIST);
			}
		});
	}

	public void testRetainAllRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.retainAll(Collections.EMPTY_LIST);
			}
		});
	}

	public void testClearRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(new Runnable() {
			public void run() {
				WritableList list = new WritableList();
				list.clear();
			}
		});
	}

	public void testNullElementType() throws Exception {
		RealmTester.setDefault(SWTObservables.getRealm(Display.getDefault()));
		WritableList writableList = new WritableList();
		assertNull(writableList.getElementType());

		writableList = new WritableList(Realm.getDefault());
		assertNull(writableList.getElementType());
	}

	public void testWithElementType() throws Exception {
		RealmTester.setDefault(SWTObservables.getRealm(Display.getDefault()));

		Object elementType = String.class;
		WritableList list = WritableList.withElementType(elementType);
		assertNotNull(list);
		assertEquals(Realm.getDefault(), list.getRealm());
		assertEquals(elementType, list.getElementType());
	}

	public static Test suite() {
		Delegate delegate = new Delegate();
		return new SuiteBuilder().addTests(WritableListTest.class)
				.addObservableContractTest(ObservableListContractTest.class,
						delegate).addObservableContractTest(
						MutableObservableListContractTest.class, delegate)
				.build();
	}

	/* package */static class Delegate extends
			AbstractObservableCollectionContractDelegate {
		public Object createElement(IObservableCollection collection) {
			return String.valueOf(collection.size() + 1);
		}

		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}

		public IObservableCollection createObservableCollection(Realm realm,
				final int itemCount) {
			WritableList observable = new WritableList(realm, new ArrayList(), String.class);

			for (int i = 0; i < itemCount; i++) {
				observable.add(String.valueOf(i));
			}

			return observable;
		}

		public void change(IObservable observable) {
			((WritableList) observable).add("");
		}
	}
}
