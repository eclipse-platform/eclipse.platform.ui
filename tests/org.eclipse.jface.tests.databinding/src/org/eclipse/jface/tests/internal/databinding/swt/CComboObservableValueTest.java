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
 *     Eric Rizzo - bug 134884
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.internal.databinding.swt.CComboObservableValue;
import org.eclipse.jface.internal.databinding.swt.SWTProperties;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;

/**
 * @since 3.2
 */
public class CComboObservableValueTest extends AbstractSWTTestCase {
	public void testDispose() throws Exception {
		CCombo combo = new CCombo(getShell(), SWT.NONE);
		CComboObservableValue observableValue = new CComboObservableValue(
				combo, SWTProperties.TEXT);

		ValueChangeEventTracker testCounterValueChangeListener = new ValueChangeEventTracker();
		observableValue.addValueChangeListener(testCounterValueChangeListener);

		assertEquals("", combo.getText());
		assertEquals("", observableValue.getValue());

		String expected1 = "Test123";
		combo.setText(expected1);

		assertEquals(1, testCounterValueChangeListener.count);
		assertEquals(expected1, combo.getText());
		assertEquals(expected1, observableValue.getValue());

		observableValue.dispose();

		String expected2 = "NewValue123";
		combo.setText(expected2);

		assertEquals(1, testCounterValueChangeListener.count);
		assertEquals(expected2, combo.getText());
	}

	public void testSetValueWithNull() {
		testSetValueWithNull(SWTProperties.TEXT);
		testSetValueWithNull(SWTProperties.SELECTION);
	}

	protected void testSetValueWithNull(String observableMode) {
		CCombo combo = new CCombo(getShell(), SWT.NONE);
		combo.setItems(new String[] {"one", "two", "three"});
		CComboObservableValue observable = new CComboObservableValue(
				combo, observableMode);

		observable.doSetValue("two");
		assertEquals("two", combo.getText());
		assertEquals(1, combo.getSelectionIndex());

		observable.doSetValue(null);
		assertEquals("", combo.getText());
		assertEquals(-1, combo.getSelectionIndex());
	}
}
