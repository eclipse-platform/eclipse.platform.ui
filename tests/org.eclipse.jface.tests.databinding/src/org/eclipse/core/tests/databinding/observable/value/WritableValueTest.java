/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - bug 158687
 *     Brad Reynolds - bug 164653
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.value;

import junit.framework.Test;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.conformance.databinding.AbstractObservableValueContractDelegate;
import org.eclipse.jface.conformance.databinding.MutableObservableValueContractTest;
import org.eclipse.jface.conformance.databinding.ObservableValueContractTest;
import org.eclipse.jface.conformance.databinding.SuiteBuilder;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;

/**
 * @since 3.2
 */
public class WritableValueTest extends AbstractDefaultRealmTestCase {
	/**
	 * All constructors delegate to the 3 arg constructor.
	 * 
	 * @throws Exception
	 */
	public void testConstructor() throws Exception {
		WritableValue value = new WritableValue(SWTObservables.getRealm(Display
				.getDefault()));
		assertNull(value.getValue());
		assertNull(value.getValueType());
	}

	public void testWithValueType() throws Exception {
		Object elementType = String.class;
		WritableValue value = WritableValue.withValueType(elementType);
		assertNotNull(value);
		assertEquals(Realm.getDefault(), value.getRealm());
		assertEquals(elementType, value.getValueType());
	}

	public static Test suite() {
		Delegate delegate = new Delegate();

		return new SuiteBuilder().addTests(WritableValueTest.class)
				.addObservableContractTest(ObservableValueContractTest.class,
						delegate).addObservableContractTest(
						MutableObservableValueContractTest.class, delegate)
				.build();
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		public IObservableValue createObservableValue(Realm realm) {
			return new WritableValue(realm, "", String.class);
		}

		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(createValue(observableValue));
		}

		public Object getValueType(IObservableValue observable) {
			return String.class;
		}
		
		public Object createValue(IObservableValue observable) {
			return observable.getValue() + "a";
		}
	}
}
