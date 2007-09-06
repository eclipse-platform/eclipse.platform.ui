/*******************************************************************************
 * Copyright (c) 2007 Ashley Cambrell and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ashley Cambrell - initial API and implementation (bug 198903)
 ******************************************************************************/
package org.eclipse.jface.tests.internal.databinding.internal.swt;

import org.eclipse.jface.internal.databinding.internal.swt.CComboSingleSelectionObservableValue;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;

/**
 * @since 3.2
 *
 */
public class CComboSingleSelectionObservableValueTest extends
		AbstractSWTTestCase {
	public void testSetValue() throws Exception {
		CCombo combo = new CCombo(getShell(), SWT.NONE);
		CComboSingleSelectionObservableValue observableValue = new CComboSingleSelectionObservableValue(
				combo);
		combo.add("Item1");
		combo.add("Item2");

		assertEquals(-1, combo.getSelectionIndex());
		assertEquals(-1, ((Integer) observableValue.getValue()).intValue());

		Integer value = new Integer(1);
		observableValue.setValue(value);
		assertEquals("combo selection index", value.intValue(), combo
				.getSelectionIndex());
		assertEquals("observable value", value, observableValue.getValue());

		assertEquals("Item2", combo.getText());
	}
}
