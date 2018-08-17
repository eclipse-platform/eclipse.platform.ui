/*******************************************************************************
 * Copyright (c) 2006, 2018 Brad Reynolds and others.
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
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.value;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.AbstractObservableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 */
public class AbstractObservableValueTest {

	@Before
	public void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
	}

	@After
	public void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}

	@Test
	public void testSetValueRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			ObservableValueStub observable = new ObservableValueStub();
			try {
				observable.setValue(null);
			} catch (UnsupportedOperationException e) {
				// do nothing
			}
		});
	}

	@Test
	public void testSetValueInvokesDoSetValue() throws Exception {
		class ValueStub extends ObservableValueStub {
			int doSetValue;

			ValueStub(Realm realm) {
				super(realm);
			}

			@Override
			protected void doSetValue(Object value) {
				doSetValue++;
			}
		}

		Realm realm = new CurrentRealm(true);
		ValueStub stub = new ValueStub(realm);
		assertEquals(0, stub.doSetValue);
		stub.setValue(new Object());
		assertEquals("doSetValue should have been invoked", 1, stub.doSetValue);
	}

	@Test
	public void testFireValueChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			ObservableValueStub observable = new ObservableValueStub();
			observable.fireValueChange(null);
		});
	}

	private static class ObservableValueStub extends AbstractObservableValue {
		ObservableValueStub() {
			super(Realm.getDefault());
		}

		private ObservableValueStub(Realm realm) {
			super(realm);
		}

		@Override
		protected Object doGetValue() {
			return null;
		}

		@Override
		public Object getValueType() {
			return null;
		}

		@Override
		protected void fireValueChange(ValueDiff diff) {
			super.fireValueChange(diff);
		}
	}
}
