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
package org.eclipse.jface.tests.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.widgets.TableColumnFactory;
import org.eclipse.jface.widgets.WidgetFactory;
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
	public void createsTableColumn() {
		TableColumn tableColumn = TableColumnFactory.newTableColumn(SWT.CENTER).create(table);

		assertEquals(table.getColumn(0), tableColumn);
		assertEquals(tableColumn.getParent(), table);
		assertEquals(SWT.CENTER, tableColumn.getStyle() & SWT.CENTER);
	}

	@Test
	public void setsSelectionListener() {
		final SelectionEvent[] raisedEvents = new SelectionEvent[1];
		TableColumn tableColumn = TableColumnFactory.newTableColumn(SWT.NONE).onSelect(e -> raisedEvents[0] = e)
				.create(table);

		tableColumn.notifyListeners(SWT.Selection, new Event());

		assertEquals(1, tableColumn.getListeners(SWT.Selection).length);
		assertNotNull(raisedEvents[0]);
	}

	@Test
	public void setsAlignment() {
		TableColumn tableColumn = TableColumnFactory.newTableColumn(SWT.NONE).align(SWT.LEFT).create(table);

		assertEquals(SWT.LEFT, tableColumn.getAlignment());
	}

	@Test
	public void setsToolTip() {
		TableColumn tableColumn = TableColumnFactory.newTableColumn(SWT.NONE).tooltip("tooltip").create(table);

		assertEquals("tooltip", tableColumn.getToolTipText());
	}

	@Test
	public void setsWidth() {
		TableColumn tableColumn = TableColumnFactory.newTableColumn(SWT.NONE).width(10).create(table);

		assertEquals(10, tableColumn.getWidth());
	}

	@Test
	public void setsMoveable() {
		TableColumn column = TableColumnFactory.newTableColumn(SWT.NONE).moveable(true).create(table);

		assertTrue(column.getMoveable());
	}

	@Test
	public void setsResizable() {
		TableColumn column = TableColumnFactory.newTableColumn(SWT.NONE).resizable(true).create(table);

		assertTrue(column.getResizable());
	}
}
