/*******************************************************************************
 * Copyright (c) 2017 vogella GmbH and others.
 *
 * This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.forms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.e4.ui.tests.css.swt.CSSSWTTestCase;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.junit.Test;

public class ExpandableCompositeTest extends CSSSWTTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);

	protected ExpandableComposite createTestExpandableComposite(String styleSheet) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite compositeToTest = new Composite(shell, SWT.NONE);
		compositeToTest.setLayout(new FillLayout());

		ExpandableComposite test = new ExpandableComposite(shell, SWT.NONE,
				ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);

		// Apply styles
		engine.applyStyles(shell, true);

		shell.pack();
		return test;
	}

	@Test
	public void testExpandableCompositeColor() {
		ExpandableComposite compositeToTest = createTestExpandableComposite(
				"ExpandableComposite { swt-titlebar-color: #FF0000; }");
		assertNotNull(compositeToTest.getTitleBarForeground());
		assertEquals(RED, compositeToTest.getTitleBarForeground().getRGB());
	}

	@Test
	public void testExpandableComposite_foregroundColorGetsReset_foregroundCollorIsNull() throws Exception {
		ExpandableComposite compositeToTest = createTestExpandableComposite(
				"ExpandableComposite { swt-titlebar-color: #FF0000; }");
		assertNotNull(compositeToTest.getTitleBarForeground());
		assertEquals(RED, compositeToTest.getTitleBarForeground().getRGB());

		engine.reset();

		assertNull(compositeToTest.getTitleBarForeground());
	}

}