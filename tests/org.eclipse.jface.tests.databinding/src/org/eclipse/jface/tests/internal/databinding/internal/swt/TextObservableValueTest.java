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

import junit.framework.TestCase;

import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.internal.databinding.internal.swt.TextObservableValue;
import org.eclipse.jface.tests.databinding.RealmTester;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Tests to assert the inputs of the TextObservableValue constructor.
 * 
 * @since 3.2
 */
public class TextObservableValueTest extends TestCase {
	private Text text;

	protected void setUp() throws Exception {
		super.setUp();

		RealmTester.setDefault(SWTObservables.getRealm(Display.getDefault()));
		Shell shell = new Shell();
		text = new Text(shell, SWT.NONE);
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
}
