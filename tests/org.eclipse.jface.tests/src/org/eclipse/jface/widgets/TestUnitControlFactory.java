/*******************************************************************************
 * Copyright (c) 2018 SAP SE and others.
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
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 544471
 ******************************************************************************/
package org.eclipse.jface.widgets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.junit.Test;

public class TestUnitControlFactory extends AbstractFactoryTest {

	@Test
	public void createsControlWithNullsWhenNothingSet() {
		Label testLabel = TestFactory.newTest().create(shell);

		assertTrue(testLabel.getEnabled());
		assertNull(testLabel.getLayoutData());
		assertNull(testLabel.getToolTipText());

		assertEquals(testLabel, shell.getChildren()[0]);
	}

	@Test
	public void createsDifferentControlsWithSameFactory() {
		TestFactory testFactory = TestFactory.newTest();

		Label label1 = testFactory.create(shell);
		Label label2 = testFactory.create(shell);

		assertNotSame(label1, label2);
		assertEquals(label1, shell.getChildren()[0]);
		assertEquals(label2, shell.getChildren()[1]);
	}

	@Test
	public void createsControlWithProperties() {
		Font font = new Font(null, new FontData());

		Label label = TestFactory.newTest() //
				.tooltip("toolTip") //
				.enabled(false) //
				.layoutData(new GridData(GridData.FILL_BOTH)) //
				.font(font) //
				.create(shell);

		assertFalse(label.getEnabled());
		assertEquals("toolTip", label.getToolTipText());
		assertTrue(label.getLayoutData() instanceof GridData);
		assertEquals(font, label.getFont());
	}

	@Test
	public void testUniqueLayoutData() {
		GridDataFactory gridDataFactory = GridDataFactory.fillDefaults().grab(true, false);
		TestFactory factory = TestFactory.newTest().layoutData(gridDataFactory::create);

		Label label = factory.create(shell);
		Label label2 = factory.create(shell);

		assertNotSame(label.getLayoutData(), label2.getLayoutData());
	}

	static class TestFactory extends AbstractControlFactory<TestFactory, Label> {

		protected TestFactory(int style) {
			super(TestFactory.class, parent -> new Label(parent, style));
		}

		public static TestFactory newTest() {
			return new TestFactory(SWT.NONE);
		}
	}
}