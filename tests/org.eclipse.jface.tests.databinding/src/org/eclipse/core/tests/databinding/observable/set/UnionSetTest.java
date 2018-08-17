/*******************************************************************************
 * Copyright (c) 2007, 2008 Brad Reynolds and others.
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
 *     Matthew Hall - bug 213145
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.set;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.UnionSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.conformance.ObservableCollectionContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;

import junit.framework.TestSuite;

/**
 */
public class UnionSetTest {
	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(ObservableCollectionContractTest.suite(new Delegate()));
	}

	private static class Delegate extends
			AbstractObservableCollectionContractDelegate {
		private IObservableSet[] sets;

		private Delegate() {
		}

		@Override
		public void setUp() {

			super.setUp();
		}

		@Override
		public void tearDown() {
			sets = null;

			super.tearDown();
		}

		@Override
		public void change(IObservable observable) {
			sets[0].add(Integer.toString(sets[0].size()));
		}

		@Override
		public Object createElement(IObservableCollection collection) {
			return Integer.toString(collection.size());
		}

		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			sets = new IObservableSet[]{new WritableSet(realm), new WritableSet(realm)};

			IObservableSet set = new UnionSet(sets);

			for (int i = 0; i < elementCount; i++) {
				sets[0].add(Integer.toString(i));
			}

			return set;
		}
	}
}
