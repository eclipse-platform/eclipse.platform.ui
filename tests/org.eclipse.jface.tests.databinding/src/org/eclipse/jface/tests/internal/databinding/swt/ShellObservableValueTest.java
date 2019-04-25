/*******************************************************************************
 * Copyright (c) 2007, 2009 Matthew Hall and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Matthew Hall - initial API and implementation (bug 212235)
 *     Matthew Hall - bug 213145, 194734, 195222
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Shell;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestSuite;

/**
 * Tests for ShellObservableValue
 *
 * @since 1.2
 */
public class ShellObservableValueTest extends AbstractDefaultRealmTestCase {
	String oldValue;
	String newValue;
	Shell shell;
	IObservableValue observable;
	ValueChangeEventTracker tracker;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		shell = new Shell();
		observable = SWTObservables.observeText(shell);
		oldValue = "old";
		newValue = "new";
		shell.setText(oldValue);
		tracker = ValueChangeEventTracker.observe(observable);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		observable.dispose();
		observable = null;
		shell.dispose();
		shell = null;
		super.tearDown();
	}

	@Test
	public void testGetValueType() {
		assertEquals(String.class, observable.getValueType());
	}

	@Test
	public void testSetValue_FiresValueChangeEvent() {
		observable.setValue(newValue);

		assertEquals(1, tracker.count);
		assertEquals(oldValue, tracker.event.diff.getOldValue());
		assertEquals(newValue, tracker.event.diff.getNewValue());
	}

	@Test
	public void testSetValue_NullConvertedToEmptyString() {
		observable.setValue(null);

		assertEquals("", observable.getValue());
		assertEquals("", shell.getText());
	}

	@Test
	public void testShellSetText_GetValueReturnsSame() {
		assertEquals(oldValue, observable.getValue());

		shell.setText(newValue);

		assertEquals(newValue, observable.getValue());
	}

	@Test
	public void testShellSetText_NoValueChangeEvent() {
		shell.setText(newValue);
		assertEquals(0, tracker.count);
	}

	public static void addConformanceTest(TestSuite suite) {
		suite.addTest(SWTMutableObservableValueContractTest.suite(new Delegate()));
	}

	static class Delegate extends AbstractObservableValueContractDelegate {
		Shell shell;

		@Override
		public void setUp() {
			super.setUp();
			shell = new Shell();
		}

		@Override
		public void tearDown() {
			shell.dispose();
			shell = null;
			super.tearDown();
		}

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return WidgetProperties.text().observe(realm, shell);
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return String.class;
		}

		@Override
		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(createValue(observableValue));
		}

		int counter;

		@Override
		public Object createValue(IObservableValue observable) {
			return Integer.toString(counter++);
		}
	}
}
