/*******************************************************************************
 * Copyright (c) 2022 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel- initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.junit.Ignore;
import org.junit.Test;

public class ToolItemTest extends CSSSWTTestCase {

	protected ToolItem createTestToolItem(String styleSheet, int styleBit) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		var shell = new Shell(display, SWT.SHELL_TRIM);
		var layout = new FillLayout();
		shell.setLayout(layout);

		var panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		ToolBar toolBar = new ToolBar(panel, SWT.FLAT);
		var toolItemToTest = new ToolItem(toolBar, styleBit);
		toolItemToTest.setText("Some text");

		// Apply styles
		// TODO should call on shell but ToolBar is currently not styling its children
		// works in a real application via CSSSWTApplyStylesListener SWT.Skin event
		engine.applyStyles(toolItemToTest, true);
		shell.pack();
		return toolItemToTest;
	}

	@Test
	public void testBackgroundColor() {
		var toolItemToTest = createTestToolItem("ToolItem { background-color: #FF0000;}",
				SWT.PUSH);
		assertEquals(RED, toolItemToTest.getBackground().getRGB());
	}

	@Test
	public void testForegroundColor() {
		var toolItemToTest = createTestToolItem("ToolItem { color: #FF0000;}", SWT.PUSH);
		assertEquals(RED, toolItemToTest.getForeground().getRGB());
	}

	@Test
	@Ignore("Not yet implemented")
	public void testSelectedPseudo() {
		var toolItemToTest = createTestToolItem(
				"ToolItem { color: #FF0000; }\n" + "ToolItem:checked { color: #0000FF; }", SWT.PUSH);
		assertEquals(RED, toolItemToTest.getForeground().getRGB());
		toolItemToTest.setSelection(true);
		engine.applyStyles(toolItemToTest, false);
		assertEquals(BLUE, toolItemToTest.getForeground().getRGB());
	}

	@Test
	public void ensurePseudoAttributeAllowsToSelectionPushButton() {
		var toolItemToTest = createTestToolItem(
				"ToolItem[style~='SWT.CHECK'] { background-color: #FF0000; color: #0000FF }", SWT.CHECK);

		assertEquals(RED, toolItemToTest.getBackground().getRGB());
		assertEquals(BLUE, toolItemToTest.getForeground().getRGB());

		var unStyledBToolItem = createTestToolItem(
				"ToolItem[style~='SWT.PUSH'] { background-color: #FF0000; color: #0000FF }", SWT.CHECK);

		assertNotEquals(RED, unStyledBToolItem.getBackground().getRGB());
		assertNotEquals(BLUE, unStyledBToolItem.getForeground().getRGB());

	}
}
