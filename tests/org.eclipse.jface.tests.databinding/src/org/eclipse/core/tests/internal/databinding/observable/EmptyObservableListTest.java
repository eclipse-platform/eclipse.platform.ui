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
import org.eclipse.core.internal.databinding.observable.EmptyObservableList;
import org.eclipse.jface.databinding.conformance.ObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.delegate.IObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @since 3.2
 * 
 */
public class EmptyObservableListTest {
	public static Test suite() {
		TestSuite suite = new TestSuite(EmptyObservableListTest.class.getName());
		suite.addTest(ImmutableObservableListContractTest.suite(new Delegate()));
		return suite;
	}

	public static class ImmutableObservableListContractTest extends
			ObservableListContractTest {
		public static Test suite(IObservableCollectionContractDelegate delegate) {
			return new SuiteBuilder().addObservableContractTest(
					ImmutableObservableListContractTest.class, delegate)
					.build();
		}

		public ImmutableObservableListContractTest(
				IObservableCollectionContractDelegate delegate) {
			super(delegate);
		}

		public ImmutableObservableListContractTest(String name,
				IObservableCollectionContractDelegate delegate) {
			super(name, delegate);
		}

		@Override
		public void testGet_GetterCalled() {
			// disabled
		}

		@Override
		public void testSubList_GetterCalled() {
			// disabled
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
		public void testIndexOf_GetterCalled() {
			// disabled
		}

		@Override
		public void testLastIndexOf_GetterCalled() {
			// disabled
		}

		@Override
		public void testListIterator_GetterCalled() {
			// disabled
		}

		@Override
		public void testListIteratorAtIndex_GetterCalled() {
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
		public void testSize_GetterCalled() throws Exception {
			// disabled
		}

		@Override
		public void testToArray_GetterCalled() throws Exception {
			// disabled
		}

		@Override
		public void testToArrayWithObjectArray_GetterCalled() throws Exception {
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
			return new EmptyObservableList(realm, elementType);
		}

		@Override
		public Object getElementType(IObservableCollection collection) {
			return elementType;
		}
	}
}
