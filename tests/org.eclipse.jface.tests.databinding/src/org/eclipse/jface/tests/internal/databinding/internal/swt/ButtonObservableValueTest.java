/*******************************************************************************
 * Copyright (c) 2006 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Ashley Cambrell - bug 198904
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.swt;

import org.eclipse.jface.internal.databinding.internal.swt.ButtonObservableValue;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;

/**
 * @since 3.2
 *
 */
public class ButtonObservableValueTest extends AbstractSWTTestCase {

	public void testSetValue() throws Exception {
		Button button = new Button(getShell(), SWT.CHECK);
		ButtonObservableValue observableValue = new ButtonObservableValue(
				button);
		assertEquals(Boolean.FALSE, observableValue.getValue());
		assertFalse(button.getSelection());

		Boolean value = Boolean.TRUE;
		observableValue.setValue(value);
		assertTrue("button value", button.getSelection());
		assertEquals("observable value", value, observableValue.getValue());
	}

	public void testDispose() throws Exception {
		Button button = new Button(getShell(), SWT.CHECK);
		ButtonObservableValue observableValue = new ButtonObservableValue(
				button);
		TestCounterValueChangeListener testCounterValueChangeListener = new TestCounterValueChangeListener();
		observableValue.addValueChangeListener(testCounterValueChangeListener);

		assertEquals(Boolean.FALSE, observableValue.getValue());
		assertFalse(button.getSelection());

		button.setSelection(true);
		notifySelection(button);

		assertEquals(1, testCounterValueChangeListener.counter);
		assertEquals(Boolean.TRUE, observableValue.getValue());
		assertTrue(button.getSelection());

		observableValue.dispose();

		button.setSelection(false);
		notifySelection(button);

		assertEquals(1, testCounterValueChangeListener.counter);

	}
}
