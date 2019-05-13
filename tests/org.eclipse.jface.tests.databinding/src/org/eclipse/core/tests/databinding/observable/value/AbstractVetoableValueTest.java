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
import org.eclipse.core.databinding.observable.value.AbstractVetoableValue;
import org.eclipse.core.databinding.observable.value.ValueDiff;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 3.2
 */
public class AbstractVetoableValueTest {

	@Before
	public void setUp() throws Exception {
		RealmTester.setDefault(new CurrentRealm(true));
	}

	@After
	public void tearDown() throws Exception {
		RealmTester.setDefault(null);
	}

	@Test
	public void testSetValueInvokesDoSetApprovedValue() throws Exception {
		class VetoableValue extends VetoableValueStub {
			int count;
			Object value;

			VetoableValue(Realm realm) {
				super(realm);
			}

			@Override
			protected void doSetApprovedValue(Object value) {
				count++;
				this.value = value;
			}
		}

		Realm realm = new CurrentRealm(true);
		VetoableValue vetoableValue = new VetoableValue(realm);
		assertEquals(0, vetoableValue.count);
		assertEquals(null, vetoableValue.value);

		Object value = new Object();
		vetoableValue.setValue(value);
		assertEquals(1, vetoableValue.count);
		assertEquals(value, vetoableValue.value);
	}

	@Test
	public void testFireValueChangeRealmChecks() throws Exception {
		RealmTester.exerciseCurrent(() -> {
			VetoableValueStub observable = new VetoableValueStub();
			observable.fireValueChanging(null);
		});
	}

	private static class VetoableValueStub extends AbstractVetoableValue {
		VetoableValueStub() {
			this(Realm.getDefault());
		}

		VetoableValueStub(Realm realm) {
			super(realm);
		}

		@Override
		protected void doSetApprovedValue(Object value) {
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
		protected boolean fireValueChanging(ValueDiff diff) {
			return super.fireValueChanging(diff);
		}
	}
}
