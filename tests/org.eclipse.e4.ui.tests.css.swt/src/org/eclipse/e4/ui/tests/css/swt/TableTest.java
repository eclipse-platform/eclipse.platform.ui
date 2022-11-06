/*******************************************************************************
 * Copyright (c) 2017 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.junit.jupiter.api.Test;

public class TableTest extends CSSSWTTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);

	protected Table createTestTable(String styleSheet) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		Table tableToTest = new Table(panel, SWT.NONE);

		// Apply styles
		engine.applyStyles(shell, true);

		shell.pack();
		return tableToTest;
	}


	@Test
	void testTableColor() {
		Table tableToTest = createTestTable("Table { background-color: #FF0000; color: #0000FF }");
		assertEquals(RED, tableToTest.getBackground().getRGB());
		assertEquals(BLUE, tableToTest.getForeground().getRGB());
	}

	@Test
	void testTableHeaderColor() {
		Table tableToTest = createTestTable(
				"Table { swt-header-background-color: #FF0000; swt-header-color: #0000FF }");
		assertEquals(RED, tableToTest.getHeaderBackground().getRGB());
		assertEquals(BLUE, tableToTest.getHeaderForeground().getRGB());
	}

}