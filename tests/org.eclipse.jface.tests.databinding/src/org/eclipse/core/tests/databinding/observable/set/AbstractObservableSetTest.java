/*******************************************************************************
 * Copyright (c) 2007, 2008 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 213145
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.set;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.AbstractObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.jface.databinding.conformance.ObservableCollectionContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;

/**
 */
public class AbstractObservableSetTest extends TestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(AbstractObservableSetTest.class.getName());
		suite.addTest(ObservableCollectionContractTest.suite(new Delegate()));
		return suite;
	}

	private static class Delegate extends
			AbstractObservableCollectionContractDelegate {
		@Override
		public void change(IObservable observable) {
			((AbstractObservableSetStub) observable).fireSetChange(Diffs.createSetDiff(new HashSet(), new HashSet()));
		}

		@Override
		public Object createElement(IObservableCollection collection) {
			return Integer.toString(collection.size());
		}

		@Override
		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}

		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			AbstractObservableSetStub set = new AbstractObservableSetStub(realm, String.class);

			for (int i = 0; i < elementCount; i++) {
				set.getWrappedSet().add(Integer.toString(i));
			}

			return set;
		}
	}

	private static class AbstractObservableSetStub extends AbstractObservableSet {
		private Object type;
		private HashSet set;

		private AbstractObservableSetStub(Realm realm, Object type) {
			super (realm);
			set = new HashSet();
			this.type = type;
		}

		@Override
		protected Set getWrappedSet() {
			return set;
		}

		@Override
		public Object getElementType() {
			return type;
		}

		@Override
		protected void fireSetChange(SetDiff diff) {
			super.fireSetChange(diff);
		}
	}
}
