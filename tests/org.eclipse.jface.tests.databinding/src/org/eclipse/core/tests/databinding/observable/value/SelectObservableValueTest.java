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
 *     Matthew Hall - initial API and implementation (bug 249992)
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.value;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.conformance.MutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

import junit.framework.TestSuite;

/**
 * @since 3.2
 *
 */
public class SelectObservableValueTest extends AbstractDefaultRealmTestCase {
	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(MutableObservableValueContractTest.suite(new Delegate()));
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return new SelectObservableValue(realm, Object.class);
		}

		@Override
		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(createValue(observableValue));
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return Object.class;
		}

		@Override
		public Object createValue(IObservableValue observable) {
			SelectObservableValue select = (SelectObservableValue) observable;
			Object value = new Object();
			IObservableValue optionObservable = new WritableValue(select
					.getRealm(), Boolean.FALSE, Boolean.TYPE);
			select.addOption(value, optionObservable);
			return value;
		}
	}
}
