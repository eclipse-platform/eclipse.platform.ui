/*******************************************************************************
 * Copyright (c) 2006, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Ashley Cambrell - bug 198904
 *     Matthew Hall - bug 213145, 194734, 195222
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.2
 */
public class ButtonObservableValueTest extends AbstractSWTTestCase {
	private Button button;
	private ISWTObservableValue observableValue;
	private ValueChangeEventTracker listener;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Shell shell = getShell();
		button = new Button(shell, SWT.CHECK);
		observableValue = SWTObservables.observeSelection(button);
		listener = new ValueChangeEventTracker();
	}

	public void testSelection_ChangeNotifiesObservable() throws Exception {
		observableValue.addValueChangeListener(listener);
		button.setSelection(true);

		// precondition
		assertEquals(0, listener.count);
		button.notifyListeners(SWT.Selection, null);

		assertEquals("Selection event should notify observable.", 1,
				listener.count);
	}

	public void testSelection_NoChange() throws Exception {
		button.setSelection(true);
		button.notifyListeners(SWT.Selection, null);
		observableValue.addValueChangeListener(listener);

		// precondition
		assertEquals(0, listener.count);

		button.notifyListeners(SWT.Selection, null);
		assertEquals(
				"Value did not change.  Listeners should not have been notified.",
				0, listener.count);
	}

	public void testSetValue_NullConvertedToFalse() {
		button.setSelection(true);
		assertEquals(Boolean.TRUE, observableValue.getValue());

		observableValue.setValue(null);
		assertEquals(Boolean.FALSE, observableValue.getValue());
	}

	public void testDispose() throws Exception {
		ValueChangeEventTracker testCounterValueChangeListener = new ValueChangeEventTracker();
		observableValue.addValueChangeListener(testCounterValueChangeListener);

		assertEquals(Boolean.FALSE, observableValue.getValue());
		assertFalse(button.getSelection());

		button.setSelection(true);
		button.notifyListeners(SWT.Selection, null);

		assertEquals(1, testCounterValueChangeListener.count);
		assertEquals(Boolean.TRUE, observableValue.getValue());
		assertTrue(button.getSelection());

		observableValue.dispose();

		button.setSelection(false);
		button.notifyListeners(SWT.Selection, null);

		assertEquals(1, testCounterValueChangeListener.count);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ButtonObservableValueTest.class
				.getName());
		suite.addTestSuite(ButtonObservableValueTest.class);
		suite.addTest(SWTMutableObservableValueContractTest
				.suite(new Delegate()));
		return suite;
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		Shell shell;

		Button button;

		@Override
		public void setUp() {
			super.setUp();

			shell = new Shell();
			button = new Button(shell, SWT.CHECK);
		}

		@Override
		public void tearDown() {
			super.tearDown();

			shell.dispose();
		}

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return WidgetProperties.selection().observe(realm, button);
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return Boolean.TYPE;
		}

		@Override
		public void change(IObservable observable) {
			((IObservableValue) observable).setValue(Boolean
					.valueOf(changeValue(button)));
		}

		@Override
		public Object createValue(IObservableValue observable) {
			return Boolean.valueOf(changeValue(button));
		}

		private boolean changeValue(Button button) {
			return !button.getSelection();
		}
	}
}
