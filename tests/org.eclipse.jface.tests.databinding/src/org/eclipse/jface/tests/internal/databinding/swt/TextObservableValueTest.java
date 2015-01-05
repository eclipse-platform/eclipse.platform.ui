/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920, 164653
 *     Ashley Cambrell - bug 198904
 *     Matthew Hall - bug 194734, 195222, 256543
 *******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Tests to assert the inputs of the TextObservableValue constructor.
 *
 * @since 3.2
 */
public class TextObservableValueTest extends AbstractDefaultRealmTestCase {
	private Text text;
	private ValueChangeEventTracker listener;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Shell shell = new Shell();
		text = new Text(shell, SWT.NONE);

		listener = new ValueChangeEventTracker();
	}

	/**
	 * Asserts that only valid SWT event types are accepted on construction of
	 * TextObservableValue.
	 */
	public void testConstructorUpdateEventTypes() {
		try {
			WidgetProperties.text(SWT.None);
			WidgetProperties.text(SWT.FocusOut);
			WidgetProperties.text(SWT.Modify);
			WidgetProperties.text(SWT.DefaultSelection);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}

		try {
			WidgetProperties.text(SWT.Verify);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=171132
	 *
	 * @throws Exception
	 */
	public void testGetValueBeforeFocusOutChangeEventsFire() throws Exception {
		IObservableValue observableValue = WidgetProperties.text(SWT.FocusOut)
				.observe(Realm.getDefault(), text);
		observableValue.addValueChangeListener(listener);

		String a = "a";
		String b = "b";

		text.setText(a);

		assertEquals(0, listener.count);

		// fetching the value updates the buffered value
		assertEquals(a, observableValue.getValue());
		assertEquals(1, listener.count);

		text.setText(b);

		assertEquals(1, listener.count);

		text.notifyListeners(SWT.FocusOut, null);

		assertEquals(2, listener.count);
		assertEquals(a, listener.event.diff.getOldValue());
		assertEquals(b, listener.event.diff.getNewValue());
	}

	public void testDispose() throws Exception {
		IObservableValue observableValue = WidgetProperties.text(SWT.Modify)
				.observe(Realm.getDefault(), text);
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
