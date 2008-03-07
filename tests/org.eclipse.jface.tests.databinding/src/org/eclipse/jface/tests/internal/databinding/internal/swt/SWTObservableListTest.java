/*******************************************************************************
 * Copyright (c) 2007 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 208858)
 *     Matthew Hall - bug 213145
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.swt;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.MutableObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.internal.databinding.internal.swt.SWTObservableList;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.3
 */
public class SWTObservableListTest extends AbstractDefaultRealmTestCase {
	SWTObservableListStub list;

	protected void setUp() throws Exception {
		super.setUp();
		list = new SWTObservableListStub(Realm.getDefault(), 0);
	}

	public void testMove_ForwardAndBackward() {
		String element0 = "element0";
		String element1 = "element1";

		list.add(element0);
		list.add(element1);

		// move forward
		assertEquals(element0, list.move(0, 1));
		assertEquals(element1, list.move(0, 1));

		// move backward
		assertEquals(element1, list.move(1, 0));
		assertEquals(element0, list.move(1, 0));
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(SWTObservableListTest.class.toString());
		suite.addTestSuite(SWTObservableListTest.class);
		suite.addTest(MutableObservableListContractTest.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			return new SWTObservableListStub(realm, elementCount);
		}

		private int counter;

		public Object createElement(IObservableCollection collection) {
			return "Item" + counter++;
		}

		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}

		public void change(IObservable observable) {
			((SWTObservableListStub) observable).fireChange();
		}
	}

	static class SWTObservableListStub extends SWTObservableList {
		String[] items;

		public SWTObservableListStub(Realm realm, int elementCount) {
			super(realm);
			items = new String[elementCount];
			for (int i = 0; i < items.length; i++)
				items[i] = Integer.toString(i);
		}

		protected String getItem(int index) {
			return items[index];
		}

		protected int getItemCount() {
			return items.length;
		}

		protected String[] getItems() {
			return items;
		}

		protected void setItem(int index, String string) {
			items[index] = string;
		}

		protected void setItems(String[] newItems) {
			items = newItems;
		}

		protected void fireChange() {
			super.fireChange();
		}
	}
}
