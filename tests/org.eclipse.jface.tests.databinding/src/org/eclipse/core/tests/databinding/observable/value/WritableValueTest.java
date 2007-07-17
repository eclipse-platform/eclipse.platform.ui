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
import org.eclipse.jface.conformance.databinding.ObservableValueContractTests;
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
		Object[] params = new Object[] { new Delegate() };

		return new SuiteBuilder().addTests(WritableValueTest.class)
				.addParameterizedTests(ObservableValueContractTests.class,
						params).build();
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		private WritableValue current;

		public IObservableValue createObservableValue() {
			Realm.runWithDefault(SWTObservables.getRealm(Display.getDefault()),
					new Runnable() {
						public void run() {
							current = new WritableValue("", String.class);
						}
					});

			return current;
		}

		public void change(IObservable observable) {
			WritableValue writableValue = (WritableValue) observable;
			writableValue.setValue(writableValue.getValue() + "a");
		}

		public Object getValueType(IObservableValue observable) {
			return String.class;
		}
	}
}
