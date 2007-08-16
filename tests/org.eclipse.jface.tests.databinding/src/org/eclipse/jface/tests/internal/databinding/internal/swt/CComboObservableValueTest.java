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

import org.eclipse.jface.internal.databinding.internal.swt.CComboObservableValue;
import org.eclipse.jface.internal.databinding.internal.swt.SWTProperties;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;

/**
 * @since 3.2
 */
public class CComboObservableValueTest extends AbstractSWTTestCase {
	public void testSetValueText() throws Exception {
		CCombo combo = new CCombo(getShell(), SWT.NONE);
		CComboObservableValue observableValue = new CComboObservableValue(
				combo, SWTProperties.TEXT);
		assertEquals("", combo.getText());
		assertEquals("", observableValue.getValue());

		String value = "value";
		observableValue.setValue(value);
		assertEquals("combo value", value, combo.getText());
		assertEquals("observable value value is incorrect", value,
				observableValue.getValue());
	}

	public void testDispose() throws Exception {
		CCombo combo = new CCombo(getShell(), SWT.NONE);
		CComboObservableValue observableValue = new CComboObservableValue(
				combo, SWTProperties.TEXT);

		TestCounterValueChangeListener testCounterValueChangeListener = new TestCounterValueChangeListener();
		observableValue.addValueChangeListener(testCounterValueChangeListener);

		assertEquals("", combo.getText());
		assertEquals("", observableValue.getValue());

		String expected1 = "Test123";
		combo.setText(expected1);

		assertEquals(1, testCounterValueChangeListener.counter);
		assertEquals(expected1, combo.getText());
		assertEquals(expected1, observableValue.getValue());

		observableValue.dispose();

		String expected2 = "NewValue123";
		combo.setText(expected2);

		assertEquals(1, testCounterValueChangeListener.counter);
		assertEquals(expected2, combo.getText());
	}
}
