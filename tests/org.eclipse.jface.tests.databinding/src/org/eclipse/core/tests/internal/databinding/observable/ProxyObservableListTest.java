/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 208332)
 *     Matthew Hall - bug 213145
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.internal.databinding.observable.ProxyObservableList;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;

/**
 * @since 3.2
 * 
 */
public class ProxyObservableListTest {
	public static Test suite() {
		TestSuite suite = new TestSuite(ProxyObservableListTest.class.getName());
		suite.addTest(ObservableListContractTest.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		private Object elementType = Object.class;

		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			IObservableList wrappedList = new WritableList(realm,
					new ArrayList(), elementType);
			for (int i = 0; i < elementCount; i++)
				wrappedList.add(new Object());
			return new ProxyObservableListStub(wrappedList);
		}

		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		public Object getElementType(IObservableCollection collection) {
			return elementType;
		}

		public void change(IObservable observable) {
			((ProxyObservableListStub) observable).wrappedList
					.add(new Object());
		}
	}

	static class ProxyObservableListStub extends ProxyObservableList {
		IObservableList wrappedList;

		ProxyObservableListStub(IObservableList wrappedList) {
			super(wrappedList);
			this.wrappedList = wrappedList;
		}
	}
}