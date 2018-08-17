/*******************************************************************************
 * Copyright (c) 2007, 2009 Ashley Cambrell and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ashley Cambrell - initial API and implementation (bug 198903)
 *     Matthew Hall - bug 194734
 ******************************************************************************/
package org.eclipse.jface.tests.internal.databinding.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class ComboSingleSelectionObservableValueTest extends
		AbstractSWTTestCase {
	@Test
	public void testSetValue() throws Exception {
		Combo combo = new Combo(getShell(), SWT.NONE);
		IObservableValue observableValue = SWTObservables
				.observeSingleSelectionIndex(combo);
		combo.add("Item1");
		combo.add("Item2");

		assertEquals(-1, combo.getSelectionIndex());
		assertEquals(-1, ((Integer) observableValue.getValue()).intValue());

		Integer value = Integer.valueOf(1);
		observableValue.setValue(value);
		assertEquals("combo selection index", value.intValue(), combo
				.getSelectionIndex());
		assertEquals("observable value", value, observableValue.getValue());

		assertEquals("Item2", combo.getText());
	}
}
