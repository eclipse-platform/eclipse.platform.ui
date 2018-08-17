/*******************************************************************************
 * Copyright (c) 2007-2008 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 212518)
 *     Matthew Hall - bug 146397
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.internal.databinding.observable.ConstantObservableValue;
import org.eclipse.jface.databinding.conformance.ObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.delegate.IObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.junit.Test;

import junit.framework.TestSuite;

/**
 * Tests for ConstantObservableValue
 *
 * @since 1.1
 */
public class ConstantObservableValueTest extends AbstractDefaultRealmTestCase {

	@Test(expected = RuntimeException.class)
	public void testConstructor_NullRealm() {
		new ConstantObservableValue(null, null, null);
	}

	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(new SuiteBuilder()
				.addObservableContractTest(UnchangeableObservableValueContractTest.class, new Delegate()).build());
	}

	private static class Delegate extends
			AbstractObservableValueContractDelegate {
		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return new ConstantObservableValue(realm, new Object(),
					Object.class);
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return Object.class;
		}
	}

	/**
	 * Non-API--this class is public so that SuiteBuilder can access it.
	 */
	public static class UnchangeableObservableValueContractTest extends
			ObservableValueContractTest {
		public UnchangeableObservableValueContractTest(String name,
				IObservableValueContractDelegate delegate) {
			super(name, delegate);
		}

		@Override
		@Test
	public void testChange_OrderOfNotifications() {
			// disabled
		}

		@Override
		@Test
	public void testChange_ValueChangeEvent() {
			// disabled
		}

		@Override
		@Test
	public void testChange_ValueChangeEventDiff() {
			// disabled
		}

		@Override
		@Test
	public void testChange_ValueChangeEventFiredAfterValueIsSet() {
			// disabled
		}

		@Override
		@Test
	public void testRemoveValueChangeListener_RemovesListener()
				throws Exception {
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
	public void testIsStale_RealmChecks() {
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
}
