/*******************************************************************************
 * Copyright (c) 2006-2008 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Brad Reynolds - bug 147515
 *     Matthew Hall - bug 221351
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.observable.masterdetail;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.core.databinding.observable.masterdetail.MasterDetailObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.masterdetail.DetailObservableValue;
import org.eclipse.jface.databinding.conformance.MutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.ObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.util.CurrentRealm;
import org.eclipse.jface.databinding.conformance.util.RealmTester;
import org.eclipse.jface.databinding.conformance.util.SuiteBuilder;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * @since 3.2
 */
public class DetailObservableValueTest extends AbstractDefaultRealmTestCase {
	private WritableValue outerObservable;

	protected void setUp() throws Exception {
		super.setUp();
		outerObservable = new WritableValue();
	}

	public void testSetValue() throws Exception {
		WritableValueFactory factory = new WritableValueFactory();
		outerObservable.setValue("");

		IObservableValue detailObservable = MasterDetailObservables
				.detailValue(outerObservable, factory, null);
		WritableValue innerObservable = factory.innerObservable;
		Object value = new Object();

		assertFalse(value.equals(innerObservable.getValue()));
		detailObservable.setValue(value);
		assertEquals("inner value", value, innerObservable.getValue());
	}

	public void testGetValueType() throws Exception {
		DetailObservableValue detailObservable = new DetailObservableValue(outerObservable, null, String.class);
		assertEquals(String.class, detailObservable.getValueType());
	}
	
	/**
	 * Asserts that when a null value type is set for the detail observable no
	 * type checking is performed and the value type is always <code>null</code>.
	 */
	public void testGetValueTypeNullValueType() throws Exception {
		WritableValueFactory factory = new WritableValueFactory();
		DetailObservableValue detailObservable = new DetailObservableValue(
				outerObservable, factory, null);
		assertNull(detailObservable.getValueType());
		factory.type = String.class;

		// force the inner observable to be recreated
		outerObservable.setValue("1");
		assertNull("value type should be ignored", detailObservable.getValueType());
		
		factory.type = Object.class;

		// force the inner observable to be recreated
		outerObservable.setValue("2");
		assertNull("value type should be ignored", detailObservable.getValueType());
	}
	
	/**
	 * Factory that creates WritableValues with the target as the value.
	 */
	static class WritableValueFactory implements IObservableFactory {
		Realm realm;
		WritableValue innerObservable;
		Object type;

		public IObservable createObservable(Object target) {
			return innerObservable = new WritableValue(realm == null ? Realm
					.getDefault() : realm, target, type);
		}
	}

	public static Test suite() {
		return new SuiteBuilder().addTests(DetailObservableValueTest.class)
				.addObservableContractTest(ObservableValueContractTest.class,
						new Delegate()).addObservableContractTest(
						MutableObservableValueContractTest.class,
						new Delegate()).build();
	}

	private static class DetailObservableValueStub extends
			DetailObservableValue {
		IObservableValue outerObservableValue;

		DetailObservableValueStub(IObservableValue outerObservableValue,
				IObservableFactory valueFactory, Object detailType) {
			super(outerObservableValue, valueFactory, detailType);
			this.outerObservableValue = outerObservableValue;
		}
	}

	private static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Object valueType;
		private Realm previousRealm;

		public void setUp() {
			super.setUp();
			valueType = new Object();
			previousRealm = Realm.getDefault();

			RealmTester.setDefault(new CurrentRealm());
		}

		public void tearDown() {
			RealmTester.setDefault(previousRealm);
			super.tearDown();
		}

		public IObservableValue createObservableValue(Realm realm) {
			WritableValueFactory valueFactory = new WritableValueFactory();
			valueFactory.realm = realm;
			valueFactory.type = valueType;
			WritableValue masterObservableValue = new WritableValue(realm,
					new Object(), null);
			return new DetailObservableValueStub(masterObservableValue,
					valueFactory, valueType);
		}

		public Object createValue(IObservableValue observable) {
			return new Object();
		}

		public Object getValueType(IObservableValue observable) {
			return valueType;
		}

		public void change(IObservable observable) {
			DetailObservableValueStub value = (DetailObservableValueStub) observable;
			value.outerObservableValue.setValue(createValue(value));
		}
	}
}
