/*******************************************************************************
 * Copyright (c) 2007 Ashley Cambrell and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ashley Cambrell - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.internal.swt;

import org.eclipse.jface.internal.databinding.internal.swt.ListSingleSelectionObservableValue;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.List;

/**
 * @since 3.2
 *
 */
public class ListSingleSelectionObservableValueTest extends AbstractSWTTestCase {
	public void testSetValue() throws Exception {
		List list = new List(getShell(), SWT.NONE);
		ListSingleSelectionObservableValue observableValue = new ListSingleSelectionObservableValue(
				list);
		list.add("Item1");

		assertEquals(-1, list.getSelectionIndex());
		assertEquals(-1, ((Integer) observableValue.getValue()).intValue());

		Integer value = new Integer(0);
		observableValue.setValue(value);
		assertEquals("list selection index", value.intValue(), list
				.getSelectionIndex());
		assertEquals("observable value", value, observableValue.getValue());
	}

	public void testDispose() throws Exception {
		List list = new List(getShell(), SWT.NONE);
		ListSingleSelectionObservableValue observableValue = new ListSingleSelectionObservableValue(
				list);
		list.add("Item1");
		list.add("Item2");

		assertEquals(-1, list.getSelectionIndex());
		assertEquals(-1, ((Integer) observableValue.getValue()).intValue());

		list.select(0);
		list.notifyListeners(SWT.Selection, null);
		assertEquals(0, list.getSelectionIndex());
		assertEquals(new Integer(0), observableValue.getValue());

		observableValue.dispose();

		list.select(1);
		list.notifyListeners(SWT.Selection, null);
		assertEquals(1, list.getSelectionIndex());
	}
}
