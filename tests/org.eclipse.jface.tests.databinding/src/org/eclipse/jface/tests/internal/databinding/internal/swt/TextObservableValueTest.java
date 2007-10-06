/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Brad Reynolds - bug 116920
 *     Brad Reynolds - bug 164653
 *     Ashley Cambrell - bug 198904
 *******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.swt;

import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.internal.databinding.internal.swt.TextObservableValue;
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
			new TextObservableValue(text, SWT.NONE);
			new TextObservableValue(text, SWT.FocusOut);
			new TextObservableValue(text, SWT.Modify);
			assertTrue(true);
		} catch (IllegalArgumentException e) {
			fail();
		}

		try {
			new TextObservableValue(text, SWT.Verify);
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
		TextObservableValue observableValue = new TextObservableValue(text, SWT.FocusOut);
		observableValue.addValueChangeListener(listener);
		
		String a = "a";
		String b = "b";
		
		text.setText(a);
		assertEquals(a, observableValue.getValue()); //fetch the value updating the buffered value
		
		text.setText(b);
		text.notifyListeners(SWT.FocusOut, null);
		
		assertEquals(1, listener.count);
		assertEquals(a, listener.event.diff.getOldValue());
		assertEquals(b, listener.event.diff.getNewValue());
	}

	public void testDispose() throws Exception {
		TextObservableValue observableValue = new TextObservableValue(text,
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
