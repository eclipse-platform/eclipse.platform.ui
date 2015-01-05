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
 *     Matthew Hall - bugs 194734, 256543
 *******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.swt.StyledTextTextProperty;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests to assert the inputs of the StyledTextObservableValue constructor.
 */
public class StyledTextObservableValueTest extends AbstractDefaultRealmTestCase {
	private StyledText text;
	private ValueChangeEventTracker listener;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Shell shell = new Shell();
		text = new StyledText(shell, SWT.NONE);

		listener = new ValueChangeEventTracker();
	}

	/**
	 * Asserts that only valid SWT event types are accepted on construction of
	 * StyledTextObservableValue.
	 */
	public void testConstructorUpdateEventTypes() {
		try {
			new StyledTextTextProperty(new int[] { SWT.None });
			new StyledTextTextProperty(new int[] { SWT.FocusOut });
			new StyledTextTextProperty(new int[] { SWT.Modify });
			new StyledTextTextProperty(new int[] { SWT.DefaultSelection });
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}

		try {
			new StyledTextTextProperty(new int[] { SWT.Verify });
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	/**
	 * s
	 *
	 * @throws Exception
	 */
	public void testGetValueBeforeFocusOutChangeEventsFire() throws Exception {
		IObservableValue observableValue = SWTObservables.observeText(text,
				SWT.FocusOut);
		observableValue.addValueChangeListener(listener);

		String a = "a";
		String b = "b";

		text.setText(a);

		// fetching the value updates the buffered value
		assertEquals(a, observableValue.getValue());
		assertEquals(1, listener.count);

		text.setText(b);

		text.notifyListeners(SWT.FocusOut, null);

		assertEquals(2, listener.count);
		assertEquals(a, listener.event.diff.getOldValue());
		assertEquals(b, listener.event.diff.getNewValue());
	}

	public void testDispose() throws Exception {
		IObservableValue observableValue = SWTObservables.observeText(text,
				SWT.Modify);
		ValueChangeEventTracker testCounterValueChangeListener = new ValueChangeEventTracker();
		observableValue.addValueChangeListener(testCounterValueChangeListener);

		String expected1 = "Test123";
		text.setText(expected1);

		assertEquals(1, testCounterValueChangeListener.count);
		assertEquals(expected1, text.getText());
		assertEquals(expected1, observableValue.getValue());

		observableValue.dispose();

		String expected2 = "NewValue123";
		text.setText(expected2);

		assertEquals(1, testCounterValueChangeListener.count);
		assertEquals(expected2, text.getText());
	}
}
