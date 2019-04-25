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
import org.eclipse.core.databinding.observable.set.AbstractObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.jface.databinding.conformance.ObservableCollectionContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;

import junit.framework.TestSuite;

/**
 */
public class AbstractObservableSetTest {
	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(ObservableCollectionContractTest.suite(new Delegate()));
	}

	private static class Delegate extends
			AbstractObservableCollectionContractDelegate<String> {
		@SuppressWarnings("unchecked")
		@Override
		public void change(IObservable observable) {
			((AbstractObservableSetStub<String>) observable)
					.fireSetChange(Diffs.createSetDiff(new HashSet<>(), new HashSet<>()));
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
		public IObservableCollection<String> createObservableCollection(Realm realm, int elementCount) {
			AbstractObservableSetStub<String> set = new AbstractObservableSetStub<>(realm, String.class);

			for (int i = 0; i < elementCount; i++) {
				set.getWrappedSet().add(Integer.toString(i));
			}

			return set;
		}
	}

	private static class AbstractObservableSetStub<E> extends AbstractObservableSet<E> {
		private Object type;
		private HashSet<E> set;

		private AbstractObservableSetStub(Realm realm, Object type) {
			super (realm);
			set = new HashSet<>();
			this.type = type;
		}

		@Override
		protected Set<E> getWrappedSet() {
			return set;
		}

		@Override
		public Object getElementType() {
			return type;
		}

		@Override
		protected void fireSetChange(SetDiff<E> diff) {
			super.fireSetChange(diff);
		}
	}
}
