/*******************************************************************************
 * Copyright (c) 2008, 2009 Code 9 Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Code 9 Corporation - initial API and implementation
 *     Chris Aniszczyk <zx@code9.com> - bug 131435
 *     Matthew Hall - bugs 194734, 195222, 262287
 *******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.util.ChangeEventTracker;
import org.eclipse.jface.databinding.conformance.util.StaleEventTracker;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests for the FocusOut version of StyledTextObservableValue.
 */
public class StyledTextObservableValueFocusOutTest extends AbstractSWTTestCase {
	public static Test suite() {
		TestSuite suite = new TestSuite(
				StyledTextObservableValueFocusOutTest.class.toString());
		suite.addTestSuite(StyledTextObservableValueFocusOutTest.class);
		suite.addTest(SWTMutableObservableValueContractTest
				.suite(new Delegate()));
		return suite;
	}

	public void testIsStale_AfterModifyBeforeFocusOut() {
		StyledText text = new StyledText(getShell(), SWT.NONE);
		text.setText("0");

		IObservableValue observable = WidgetProperties.text(SWT.FocusOut)
				.observe(text);

		StaleEventTracker staleTracker = StaleEventTracker.observe(observable);
		ChangeEventTracker changeTracker = ChangeEventTracker
				.observe(observable);

		assertFalse(observable.isStale());
		assertEquals(0, staleTracker.count);
		assertEquals(0, changeTracker.count);

		text.setText("1");
		text.notifyListeners(SWT.Modify, null);

		assertTrue(observable.isStale());
		assertEquals(1, staleTracker.count);
		assertEquals(0, changeTracker.count);

		text.notifyListeners(SWT.FocusOut, null);

		assertFalse(observable.isStale());
		assertEquals(1, staleTracker.count);
		assertEquals(1, changeTracker.count);
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Shell shell;

		private StyledText text;

		@Override
		public void setUp() {
			shell = new Shell();
			text = new StyledText(shell, SWT.NONE);
		}

		@Override
		public void tearDown() {
			shell.dispose();
		}

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return WidgetProperties.text(SWT.FocusOut).observe(realm, text);
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return String.class;
		}

		@Override
		public void change(IObservable observable) {
			text.setFocus();

			IObservableValue observableValue = (IObservableValue) observable;
			text.setText((String) createValue(observableValue));

			text.notifyListeners(SWT.FocusOut, null);
		}

		@Override
		public Object createValue(IObservableValue observable) {
			String value = (String) observable.getValue();
			return value + "a";
		}
	}
}
