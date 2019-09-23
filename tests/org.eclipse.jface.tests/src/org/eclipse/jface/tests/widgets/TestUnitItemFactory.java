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
import static org.junit.Assert.assertNotSame;

import org.eclipse.jface.widgets.TableColumnFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.junit.Before;
import org.junit.Test;

/**
 * This test uses a TableColumnFactory to test the methods of
 * AbstractItemFactory.
 */
public class TestUnitItemFactory extends AbstractFactoryTest {

	private Table table;

	@Override
	@Before
	public void setup() {
		super.setup();
		table = new Table(shell, SWT.NONE);
	}

	@Test
	public void createsDifferentItemsWithSameFactory() {
		TableColumnFactory testFactory = TableColumnFactory.newTableColumn(SWT.NONE);

		TableColumn column1 = testFactory.create(table);
		TableColumn column2 = testFactory.create(table);

		assertNotSame(column1, column2);
		assertEquals(column1, table.getColumn(0));
		assertEquals(column2, table.getColumn(1));
	}

	@Test
	public void createsControlWithProperties() {
		TableColumn column = TableColumnFactory.newTableColumn(SWT.NONE).image(image).text("Column").create(table);

		assertEquals(image, column.getImage());
		assertEquals("Column", column.getText());
	}
}