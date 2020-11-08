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
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.TestCollection;

/**
 * @since 3.2
 *
 */
public class DecoratingObservableListTest {
	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(MutableObservableListContractTest.class, new Delegate());
		suite.addTest(ObservableListContractTest.class, new Delegate());
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate<Object> {
		private Object elementType = Object.class;

		@Override
		public IObservableCollection<Object> createObservableCollection(Realm realm,
				int elementCount) {
			IObservableList<Object> wrappedList = new WritableList<>(realm, new ArrayList<>(), elementType);
			for (int i = 0; i < elementCount; i++)
				wrappedList.add(new Object());
			return new DecoratingObservableListStub<>(wrappedList);
		}

		@Override
		public Object createElement(IObservableCollection<Object> collection) {
			return new Object();
		}

		@Override
		public Object getElementType(IObservableCollection<Object> collection) {
			return elementType;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void change(IObservable observable) {
			((DecoratingObservableListStub<Object>) observable).decorated.add(new Object());
		}
	}

	static class DecoratingObservableListStub<E> extends DecoratingObservableList<E> {
		IObservableList<E> decorated;

		DecoratingObservableListStub(IObservableList<E> decorated) {
			super(decorated, true);
			this.decorated = decorated;
		}
	}
}
