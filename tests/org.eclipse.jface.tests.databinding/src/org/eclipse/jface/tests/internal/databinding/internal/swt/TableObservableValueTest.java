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

import org.eclipse.jface.internal.databinding.internal.swt.TableSingleSelectionObservableValue;
import org.eclipse.jface.tests.databinding.AbstractSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 3.2
 */
public class TableObservableValueTest extends AbstractSWTTestCase {
	public void testDispose() throws Exception {
		Table table = new Table(getShell(), SWT.NONE);
		TableSingleSelectionObservableValue observableValue = new TableSingleSelectionObservableValue(
				table);

		TableItem item1 = new TableItem(table, SWT.NONE);
		item1.setText("Item1");
		TableItem item2 = new TableItem(table, SWT.NONE);
		item2.setText("Item2");

		assertEquals(-1, table.getSelectionIndex());
		assertEquals(-1, ((Integer) observableValue.getValue()).intValue());

		table.select(0);
		table.notifyListeners(SWT.Selection, null);

		assertEquals(0, table.getSelectionIndex());
		assertEquals(new Integer(0), observableValue.getValue());

		observableValue.dispose();

		table.select(1);
		table.notifyListeners(SWT.Selection, null);
		assertEquals(1, table.getSelectionIndex());
	}
}
