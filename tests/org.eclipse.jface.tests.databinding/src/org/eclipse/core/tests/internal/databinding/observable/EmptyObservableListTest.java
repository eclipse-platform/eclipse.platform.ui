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
import org.junit.Test;

import junit.framework.TestSuite;

/**
 * @since 3.2
 *
 */
public class EmptyObservableListTest {

	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(new SuiteBuilder()
				.addObservableContractTest(ImmutableObservableListContractTest.class, new Delegate()).build());
	}

	public static class ImmutableObservableListContractTest extends
			ObservableListContractTest {

		public ImmutableObservableListContractTest(
				IObservableCollectionContractDelegate delegate) {
			super(delegate);
		}

		public ImmutableObservableListContractTest(String name,
				IObservableCollectionContractDelegate delegate) {
			super(name, delegate);
		}

		@Override
		@Test
	public void testGet_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testSubList_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testChange_ChangeEvent() {
			// disabled
		}

		@Override
		@Test
	public void testChange_EventObservable() {
			// disabled
		}

		@Override
		@Test
	public void testChange_ObservableRealmIsTheCurrentRealm() {
			// disabled
		}

		@Override
		@Test
	public void testChange_RealmCheck() {
			// disabled
		}

		@Override
		@Test
	public void testRemoveChangeListener_RemovesListener() {
			// disabled
		}

		@Override
		@Test
	public void testIndexOf_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testLastIndexOf_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testListIterator_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testListIteratorAtIndex_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testContains_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testContainsAll_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testEquals_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testHashCode_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testIsEmpty_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testIterator_GetterCalled() {
			// disabled
		}

		@Override
		@Test
	public void testSize_GetterCalled() throws Exception {
			// disabled
		}

		@Override
		@Test
	public void testToArray_GetterCalled() throws Exception {
			// disabled
		}

		@Override
		@Test
	public void testToArrayWithObjectArray_GetterCalled() throws Exception {
			// disabled
		}

		@Override
		@Test
	public void testIsStale_GetterCalled() throws Exception {
			// disabled
		}

		@Override
		@Test
	public void testIsDisposed() throws Exception {
			// disabled
		}

		@Override
		@Test
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
