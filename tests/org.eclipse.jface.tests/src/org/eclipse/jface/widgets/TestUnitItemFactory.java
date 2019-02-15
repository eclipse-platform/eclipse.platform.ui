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
import static org.junit.Assert.assertNotSame;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.junit.Before;
import org.junit.Test;

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
		TestFactory testFactory = TestFactory.newTest();

		TableColumn column1 = testFactory.create(table);
		TableColumn column2 = testFactory.create(table);

		assertNotSame(column1, column2);
		assertEquals(column1, table.getColumn(0));
		assertEquals(column2, table.getColumn(1));
	}

	@Test
	public void createsControlWithProperties() {
		TableColumn column = TestFactory.newTest() //
				.image(image).text("Column").create(table);

		assertEquals(image, column.getImage());
		assertEquals("Column", column.getText());
	}

	static class TestFactory extends AbstractItemFactory<TestFactory, TableColumn, Table> {

		protected TestFactory(int style) {
			super(TestFactory.class, parent -> new TableColumn(parent, style));
		}

		public static TestFactory newTest() {
			return new TestFactory(SWT.NONE);
		}
	}
}
