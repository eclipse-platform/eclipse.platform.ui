/*************************************************************************a******
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 213145, 195222
 *******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.ISWTObservable;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestSuite;

/**
 * @since 3.2
 */
public class CComboObservableValueSelectionTest {
	private Delegate delegate;

	private CCombo combo;

	@Before
	public void setUp() throws Exception {
		delegate = new Delegate();
		delegate.setUp();
		combo = delegate.combo;
	}

	@After
	public void tearDown() throws Exception {
		delegate.tearDown();
	}

	@Test
	public void testSelection_NotifiesObservable() throws Exception {
		@SuppressWarnings("unchecked")
		IObservableValue<Object> observable = (IObservableValue<Object>) delegate
				.createObservable(DisplayRealm.getRealm(Display.getDefault()));

		ValueChangeEventTracker<Object> listener = ValueChangeEventTracker.observe(observable);
		combo.select(0);

		assertEquals("Observable was not notified.", 1, listener.count);
	}

	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(SWTMutableObservableValueContractTest.suite(new Delegate()));
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Shell shell;

		/* package */CCombo combo;

		@Override
		public void setUp() {
			shell = new Shell();
			combo = new CCombo(shell, SWT.NONE);
			combo.add("a");
			combo.add("b");
		}

		@Override
		public void tearDown() {
			shell.dispose();
		}

		@Override
		public IObservableValue<Object> createObservableValue(Realm realm) {
			return WidgetProperties.widgetSelection().observe(realm, combo);
		}

		@Override
		public void change(IObservable observable) {
			@SuppressWarnings("unchecked")
			IObservableValue<Object> ov = (IObservableValue<Object>) observable;
			ov.setValue(createValue(ov));
		}

		@Override
		public Object getValueType(IObservableValue<?> observable) {
			return String.class;
		}

		@Override
		public Object createValue(IObservableValue<?> observable) {
			CCombo combo = ((CCombo) ((ISWTObservable) observable).getWidget());
			switch (combo.getSelectionIndex()) {
			case -1:
				// fall thru
			case 1:
				return combo.getItem(0);
			case 0:
				return combo.getItem(1);
			default:
				throw new RuntimeException("Unexpected selection.");
			}
		}
	}
}
