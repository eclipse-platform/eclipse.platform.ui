/*******************************************************************************
 * Copyright (c) 2020 Marcus Hoepfner and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marcus Hoepfner - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.junit.Test;

/**
 * This test uses a LabelFactory to test the methods of AbstractWidgetFactory.
 */
public class TestUnitWidgetFactory extends AbstractFactoryTest {
	@Test
	public void createsWidgetWithNullsWhenNothingSet() {
		Label testLabel = LabelFactory.newLabel(SWT.NONE).create(shell);

		assertNull(testLabel.getData());

		assertEquals(testLabel, shell.getChildren()[0]);
	}

	@Test
	public void createsDifferentWidgetsWithSameFactory() {
		LabelFactory testFactory = LabelFactory.newLabel(SWT.NONE);

		Label label1 = testFactory.create(shell);
		Label label2 = testFactory.create(shell);

		assertNotSame(label1, label2);
		assertEquals(label1, shell.getChildren()[0]);
		assertEquals(label2, shell.getChildren()[1]);
	}

	@Test
	public void setsData() {
		String data = "myData";
		Label testLabel = LabelFactory.newLabel(SWT.NONE).data(data).create(shell);

		assertEquals(data, testLabel.getData());
	}

	@Test
	public void setsDataWithKey() {
		String data1 = "data1";
		String data2 = "data2";
		Label testLabel = LabelFactory.newLabel(SWT.NONE).data("id1", data1).data("id2", data2).create(shell);

		assertEquals(data1, testLabel.getData("id1"));
		assertEquals(data2, testLabel.getData("id2"));
	}
}