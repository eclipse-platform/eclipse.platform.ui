/*******************************************************************************
 * Copyright (c) 2008, 2009 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 215531)
 *     Matthew Hall - bug 213145
 *         (through ObservableViewerElementSetTest.java)
 *     Matthew Hall - bug 262269
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.internal.databinding.identity.IdentityObservableSet;
import org.eclipse.jface.databinding.conformance.MutableObservableSetContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;

import junit.framework.TestSuite;

public class IdentityObservableSetTest {
	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(MutableObservableSetContractTest.suite(new Delegate()));
	}

	private static class Delegate extends
			AbstractObservableCollectionContractDelegate {

		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			IdentityObservableSet set = new IdentityObservableSet(realm,
					Object.class);
			for (int i = 0; i < elementCount; i++)
				set.add(createElement(set));
			return set;
		}

		@Override
		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		@Override
		public void change(IObservable observable) {
			IObservableSet set = (IObservableSet) observable;
			set.add(createElement(set));
		}

		@Override
		public Object getElementType(IObservableCollection collection) {
			return Object.class;
		}
	}
}
