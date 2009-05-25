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
 *     Eric Rizzo - bug 134884
 *     Matthew Hall - bug 194734, 195222
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;

/**
 * @since 3.2
 */
public class CComboObservableValueTest extends AbstractSWTTestCase {
	public void testDispose() throws Exception {
		CCombo combo = new CCombo(getShell(), SWT.NONE);
		ISWTObservableValue observableValue = SWTObservables.observeText(combo);

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
		testSetValueWithNull(WidgetProperties.text());
		testSetValueWithNull(WidgetProperties.selection());
	}

	protected void testSetValueWithNull(IValueProperty property) {
		CCombo combo = new CCombo(getShell(), SWT.NONE);
		combo.setItems(new String[] { "one", "two", "three" });
		IObservableValue observable = property.observe(Realm.getDefault(),
				combo);

		observable.setValue("two");
		assertEquals("two", combo.getText());
		assertEquals(1, combo.getSelectionIndex());

		observable.setValue(null);
		assertEquals("", combo.getText());
		assertEquals(-1, combo.getSelectionIndex());
	}
}
