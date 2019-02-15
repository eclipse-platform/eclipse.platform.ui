/*******************************************************************************
* Copyright (c) 2019 SAP SE and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     SAP SE - initial version
******************************************************************************/
package org.eclipse.jface.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.junit.Test;

public class TestUnitTableFactory extends AbstractFactoryTest {

	@Test
	public void createsTable() {
		Table table = TableFactory.newTable(SWT.CHECK).create(shell);

		assertEquals(shell, table.getParent());
		assertEquals(SWT.CHECK, table.getStyle() & SWT.CHECK);
	}

	@Test
	public void createsTableWithAllProperties() {
		final SelectionEvent[] raisedEvents = new SelectionEvent[1];

		Table table = TableFactory.newTable(SWT.NONE) //
				.headerVisible(true) //
				.linesVisible(true) //
				.itemCount(42) //
				.onSelect(e -> raisedEvents[0] = e) //
				.create(shell);

		table.notifyListeners(SWT.Selection, new Event());

		assertEquals(1, table.getListeners(SWT.Selection).length);
		assertNotNull(raisedEvents[0]);

		assertTrue(table.getHeaderVisible());
		assertTrue(table.getLinesVisible());
		assertEquals(42, table.getItemCount());
	}

}
