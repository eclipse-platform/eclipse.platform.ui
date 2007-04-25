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
 *******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.swt;

import org.eclipse.jface.internal.databinding.internal.swt.TextObservableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.jface.tests.databinding.EventTrackers.ValueChangeEventTracker;
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

	public void testSetValue() throws Exception {
		TextObservableValue observableValue = new TextObservableValue(text,
				SWT.NONE);

		assertEquals("", observableValue.getValue());
		String value = "value";
		observableValue.setValue(value);
		assertEquals("observable value", value, observableValue.getValue());
	}
	
	public void testSetValueValueChangeEvent() throws Exception {
		String a = "a";
		String b = "b";
		
		TextObservableValue observableValue = new TextObservableValue(text, SWT.NONE);
		observableValue.addValueChangeListener(listener);
		
		observableValue.setValue(a);
		assertEquals("", listener.event.diff.getOldValue());
		assertEquals(a, listener.event.diff.getNewValue());
		
		observableValue.setValue(b);
		assertEquals(a, listener.event.diff.getOldValue());
		assertEquals(b, listener.event.diff.getNewValue());
	}

	public void testOnModifyValueChangeEvent() throws Exception {
		TextObservableValue observableValue = new TextObservableValue(text,
				SWT.Modify);

		String a = "a";
		String b = "b";

		text.setText(a);
		
		observableValue.addValueChangeListener(listener);

		assertEquals(0, listener.count);
		text.setText(b);

		assertEquals(1, listener.count);
		assertEquals(a, listener.event.diff.getOldValue());
		assertEquals(b, listener.event.diff.getNewValue());
	}

	public void testOnFocusOutValueChangeEvent() throws Exception {
		String a = "a";
		String b = "b";
		
		text.setText(a);

		TextObservableValue observableValue = new TextObservableValue(text,
				SWT.FocusOut);
		
		observableValue.addValueChangeListener(listener);
		
		text.setText(b);
		assertEquals(0, listener.count);
		
		text.notifyListeners(SWT.FocusOut, null);
		assertEquals(1, listener.count);
		
		assertEquals(a, listener.event.diff.getOldValue());
		assertEquals(b, listener.event.diff.getNewValue());
	}
	
	public void testChangeEventsSuppressedWhenValueDoesNotChange() throws Exception {
		TextObservableValue observableValue = new TextObservableValue(text, SWT.Modify);
		
		observableValue.addValueChangeListener(listener);
		
		String value = "value";
		text.setText(value);
		assertEquals(1, listener.count);
		
		text.setText(value);
		assertEquals("listener not notified", 1, listener.count);
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
}
