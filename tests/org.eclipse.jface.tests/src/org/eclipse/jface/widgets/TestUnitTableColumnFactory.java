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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.junit.Before;
import org.junit.Test;

public class TestUnitTableColumnFactory extends AbstractFactoryTest {

	private Table table;

	@Override
	@Before
	public void setup() {
		super.setup();
		table = WidgetFactory.table(SWT.NONE).create(shell);
	}

	@Test
	public void createTableColumn() {
		TableColumn tableColumn = TableColumnFactory.newTableColumn(SWT.CENTER).create(table);

		assertEquals(table.getColumn(0), tableColumn);
		assertEquals(tableColumn.getParent(), table);
		assertEquals(SWT.CENTER, tableColumn.getStyle() & SWT.CENTER);
	}

	@Test
	public void createTableColumnWithAllProperties() {
		final SelectionEvent[] raisedEvents = new SelectionEvent[1];
		TableColumn tableColumn = TableColumnFactory.newTableColumn(SWT.NONE) //
				.onSelect(e -> raisedEvents[0] = e) //
				.align(SWT.LEFT) //
				.tooltip("tooltip") //
				.width(10) //
				.create(table);

		tableColumn.notifyListeners(SWT.Selection, new Event());

		assertEquals(1, tableColumn.getListeners(SWT.Selection).length);
		assertNotNull(raisedEvents[0]);

		assertEquals(SWT.LEFT, tableColumn.getAlignment());
		assertEquals("tooltip", tableColumn.getToolTipText());
		assertEquals(10, tableColumn.getWidth());
	}
}
