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

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.internal.databinding.observable.ProxyObservableSet;
import org.eclipse.jface.databinding.conformance.ObservableCollectionContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;

/**
 * @since 3.2
 * 
 */
public class ProxyObservableSetTest {
	public static Test suite() {
		TestSuite suite = new TestSuite(ProxyObservableSetTest.class.getName());
		suite.addTest(ObservableCollectionContractTest.suite(new Delegate()));
		return suite;
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		private Object elementType = Object.class;

		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			IObservableSet wrappedSet = new WritableSet(realm,
					Collections.EMPTY_SET, elementType);
			for (int i = 0; i < elementCount; i++)
				wrappedSet.add(createElement(wrappedSet));
			return new ProxyObservableSetStub(wrappedSet);
		}

		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		public Object getElementType(IObservableCollection collection) {
			return elementType;
		}

		public void change(IObservable observable) {
			ProxyObservableSetStub set = (ProxyObservableSetStub) observable;
			set.wrappedSet.add(createElement(set));
		}
	}

	static class ProxyObservableSetStub extends ProxyObservableSet {
		IObservableSet wrappedSet;

		ProxyObservableSetStub(IObservableSet wrappedSet) {
			super(wrappedSet);
			this.wrappedSet = wrappedSet;
		}
	}
}
