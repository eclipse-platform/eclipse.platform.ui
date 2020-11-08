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
 *         (through ProxyObservableSetTest.java)
 *     Matthew Hall - bug 237718
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.set;

import java.util.Collections;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.DecoratingObservableSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.conformance.MutableObservableCollectionContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.TestCollection;

/**
 * @since 3.2
 *
 */
public class DecoratingObservableSetTest {
	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(MutableObservableCollectionContractTest.class, new Delegate());
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate<Object> {
		private Object elementType = Object.class;

		@Override
		public IObservableCollection<Object> createObservableCollection(Realm realm,
				int elementCount) {
			IObservableSet<Object> wrappedSet = new WritableSet<>(realm, Collections.emptySet(), elementType);
			for (int i = 0; i < elementCount; i++)
				wrappedSet.add(createElement(wrappedSet));
			return new DecoratingObservableSetStub(wrappedSet);
		}

		@Override
		public Object createElement(IObservableCollection<Object> collection) {
			return new Object();
		}

		@Override
		public Object getElementType(IObservableCollection<Object> collection) {
			return elementType;
		}

		@Override
		public void change(IObservable observable) {
			DecoratingObservableSetStub set = (DecoratingObservableSetStub) observable;
			set.wrappedSet.add(createElement(set));
		}
	}

	static class DecoratingObservableSetStub extends DecoratingObservableSet<Object> {
		IObservableSet<Object> wrappedSet;

		DecoratingObservableSetStub(IObservableSet<Object> wrappedSet) {
			super(wrappedSet, true);
			this.wrappedSet = wrappedSet;
		}
	}
}
