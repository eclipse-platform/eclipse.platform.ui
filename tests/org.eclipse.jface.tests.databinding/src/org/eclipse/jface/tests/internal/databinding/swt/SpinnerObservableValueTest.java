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
 *     Matthew Hall - bug 194734
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import org.eclipse.jface.databinding.conformance.util.ValueChangeEventTracker;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Spinner;

/**
 * @since 3.2
 *
 */
public class SpinnerObservableValueTest extends AbstractSWTTestCase {
	public void testDispose() throws Exception {
		Spinner spinner = new Spinner(getShell(), SWT.NONE);
		ISWTObservableValue observableValue = SWTObservables.observeSelection(spinner);
		ValueChangeEventTracker testCounterValueChangeListener = new ValueChangeEventTracker();
		observableValue.addValueChangeListener(testCounterValueChangeListener);

		assertEquals(0, spinner.getSelection());
		assertEquals(0, ((Integer) observableValue.getValue()).intValue());

		Integer expected1 = Integer.valueOf(1);
		spinner.setSelection(expected1.intValue());

//		assertEquals(1, testCounterValueChangeListener.counter);
		assertEquals(expected1.intValue(), spinner.getSelection());
		assertEquals(expected1, observableValue.getValue());

		observableValue.dispose();

		Integer expected2 = Integer.valueOf(2);
		spinner.setSelection(expected2.intValue());

//		assertEquals(1, testCounterValueChangeListener.counter);
		assertEquals(expected2.intValue(), spinner.getSelection());
	}
}
