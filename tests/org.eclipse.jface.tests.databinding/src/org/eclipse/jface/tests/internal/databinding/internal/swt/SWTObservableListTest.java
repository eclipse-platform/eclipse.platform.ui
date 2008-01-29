/*******************************************************************************
 * Copyright (c) 2007 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 208858)
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.swt;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.jface.databinding.conformance.MutableObservableListContractTest;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;
import org.eclipse.jface.internal.databinding.internal.swt.SWTObservableList;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.3
 */
public class SWTObservableListTest extends AbstractDefaultRealmTestCase {
	public static Test suite() {
		return new SuiteBuilder()
				// .addTests(SWTObservableListTest.class) // no tests yet
				.addObservableContractTest(
						MutableObservableListContractTest.class, new Delegate())
				.addObservableContractTest(ObservableListContractTest.class,
						new Delegate()).addObservableContractTest(
						ObservableListContractTest.class, new Delegate())
				.build();
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
