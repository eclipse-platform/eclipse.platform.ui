/*******************************************************************************
 * Copyright (c) 2007-2008 Brad Reynolds and others.
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
 *     Matthew Hall - bugs 221351, 213145
 ******************************************************************************/
package org.eclipse.core.tests.databinding.observable.set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.jface.databinding.conformance.MutableObservableSetContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

import junit.framework.TestSuite;

/**
 */
public class WritableSetTest extends AbstractDefaultRealmTestCase {
	@Test
	public void testWithElementType() throws Exception {
		Object elementType = String.class;
		WritableSet set = WritableSet.withElementType(elementType);
		assertNotNull(set);
		assertEquals(Realm.getDefault(), set.getRealm());
		assertEquals(elementType, set.getElementType());
	}

	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(MutableObservableSetContractTest.suite(new Delegate()));
	}

	private static class Delegate extends
			AbstractObservableCollectionContractDelegate {
		private Delegate() {
			super();
		}

		@Override
		public void change(IObservable observable) {
			IObservableSet set = (IObservableSet) observable;
			set.add(createElement(set));
		}

		@Override
		public Object createElement(IObservableCollection collection) {
			return new Object();
		}

		@Override
		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}

		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			IObservableSet set = new WritableSet(realm, Collections.EMPTY_SET,
					String.class);
			for (int i = 0; i < elementCount; i++) {
				set.add(createElement(set));
			}

			return set;
		}
	}
}
