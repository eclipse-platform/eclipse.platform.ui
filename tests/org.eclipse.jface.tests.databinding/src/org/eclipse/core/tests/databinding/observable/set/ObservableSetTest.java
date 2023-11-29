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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.jface.databinding.conformance.ObservableCollectionContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.TestCollection;

/**
 * @since 1.1
 */
public class ObservableSetTest {
	public static void addConformanceTest(TestCollection suite) {
		suite.addTest(ObservableCollectionContractTest.class, new Delegate());
	}

	private static class Delegate extends AbstractObservableCollectionContractDelegate<String> {
		private Delegate() {
		}

		@Override
		public void change(IObservable observable) {
			((ObservableSetStub) observable).fireSetChange(Diffs.createSetDiff(new HashSet<>(), new HashSet<>()));
		}

		@Override
		public String createElement(IObservableCollection<String> collection) {
			return Integer.toString(collection.size());
		}

		@Override
		public Object getElementType(IObservableCollection<String> collection) {
			return String.class;
		}

		@Override
		public IObservableCollection<String> createObservableCollection(Realm realm,
				int elementCount) {
			IObservableSet<String> set = new ObservableSetStub(realm, new HashSet<>(), String.class);

			for (int i = 0; i < elementCount; i++) {
				set.add(Integer.toString(i));
			}

			return set;
		}
	}

	private static class ObservableSetStub extends ObservableSet<String> {
		protected ObservableSetStub(Realm realm, Set<String> wrappedSet, Object elementType) {
			super(realm, wrappedSet, elementType);
		}

		@Override
		public void fireSetChange(SetDiff<String> diff) {
			super.fireSetChange(diff);
		}
	}
}
