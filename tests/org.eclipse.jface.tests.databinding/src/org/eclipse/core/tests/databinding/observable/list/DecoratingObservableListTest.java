/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 208332)
 *     Matthew Hall - bug 213145
 *         (through ProxyObservableListTest.java)
 *     Matthew Hall - bug 237718
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.list;

import java.util.ArrayList;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.DecoratingObservableList;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.conformance.MutableObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;

import junit.framework.TestSuite;

/**
 * @since 3.2
 *
 */
public class DecoratingObservableListTest {
	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(MutableObservableListContractTest.suite(new Delegate()));
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		private Object elementType = Object.class;

		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			IObservableList wrappedList = new WritableList(realm,
					new ArrayList(), elementType);
			for (int i = 0; i < elementCount; i++)
				wrappedList.add(new Object());
			return new DecoratingObservableListStub(wrappedList);
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
			((DecoratingObservableListStub) observable).decorated
					.add(new Object());
		}
	}

	static class DecoratingObservableListStub extends DecoratingObservableList {
		IObservableList decorated;

		DecoratingObservableListStub(IObservableList decorated) {
			super(decorated, true);
			this.decorated = decorated;
		}
	}
}
