/*******************************************************************************
 * Copyright (c) 2008 Matthew Hall and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 208332)
 *     Matthew Hall - bugs 213145, 146397
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.internal.databinding.observable.EmptyObservableSet;
import org.eclipse.jface.databinding.conformance.ObservableCollectionContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.delegate.IObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.2
 *
 */
public class EmptyObservableSetTest {
	public static Test suite() {
		TestSuite suite = new TestSuite(EmptyObservableSetTest.class.getName());
		suite.addTest(ImmutableObservableSetContractTest.suite(new Delegate()));
		return suite;
	}

	public static class ImmutableObservableSetContractTest extends
			ObservableCollectionContractTest {
		public static Test suite(IObservableCollectionContractDelegate delegate) {
			return new SuiteBuilder().addObservableContractTest(
					ImmutableObservableSetContractTest.class, delegate).build();
		}

		public ImmutableObservableSetContractTest(
				IObservableCollectionContractDelegate delegate) {
			super(delegate);
		}

		public ImmutableObservableSetContractTest(String name,
				IObservableCollectionContractDelegate delegate) {
			super(name, delegate);
		}

		@Override
		public void testChange_ChangeEvent() {
			// disabled
		}

		@Override
		public void testChange_EventObservable() {
			// disabled
		}

		@Override
		public void testChange_ObservableRealmIsTheCurrentRealm() {
			// disabled
		}

		@Override
		public void testChange_RealmCheck() {
			// disabled
		}

		@Override
		public void testRemoveChangeListener_RemovesListener() {
			// disabled
		}

		@Override
		public void testContains_GetterCalled() {
			// disabled
		}

		@Override
		public void testContainsAll_GetterCalled() {
			// disabled
		}

		@Override
		public void testEquals_GetterCalled() {
			// disabled
		}

		@Override
		public void testHashCode_GetterCalled() {
			// disabled
		}

		@Override
		public void testIsEmpty_GetterCalled() {
			// disabled
		}

		@Override
		public void testIterator_GetterCalled() {
			// disabled
		}

		@Override
		public void testSize_GetterCalled() {
			// disabled
		}

		@Override
		public void testToArray_GetterCalled() {
			// disabled
		}

		@Override
		public void testToArrayWithObjectArray_GetterCalled() {
			// disabled
		}

		@Override
		public void testIsStale_GetterCalled() throws Exception {
			// disabled
		}

		@Override
		public void testIsDisposed() throws Exception {
			// disabled
		}

		@Override
		public void testAddDisposeListener_HandleDisposeInvoked() {
			// disabled
		}
	}

	private static class Delegate extends
			AbstractObservableCollectionContractDelegate {
		private Object elementType = new Object();

		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			return new EmptyObservableSet(realm, elementType);
		}

		@Override
		public Object getElementType(IObservableCollection collection) {
			return elementType;
		}
	}
}
