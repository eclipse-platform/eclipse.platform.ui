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
 *    Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.forms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.tests.css.swt.CssSwtEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.ToggleHyperlink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ExpandableCompositeTest {

	@RegisterExtension
	CssSwtEngine css = new CssSwtEngine();

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);

	protected ExpandableComposite createTestExpandableComposite(String styleSheet) {
		Display display = css.getDisplay();
		CSSEngine engine = css.createEngine(styleSheet);

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
	void testExpandableCompositeColor() {
		ExpandableComposite compositeToTest = createTestExpandableComposite(
				"ExpandableComposite { swt-titlebar-color: #FF0000; tb-toggle-color: #FF0000; tb-toggle-hover-color: #00FF00}");
		assertNotNull(compositeToTest.getTitleBarForeground());
		assertEquals(RED, compositeToTest.getTitleBarForeground().getRGB());

		ToggleHyperlink toggle = (ToggleHyperlink) compositeToTest.getChildren()[0];
		assertNotNull(toggle.getDecorationColor());
		assertEquals(RED, toggle.getDecorationColor().getRGB());
		assertNotNull(toggle.getHoverDecorationColor());
		assertEquals(GREEN, toggle.getHoverDecorationColor().getRGB());
	}

	@Test
	void testExpandableComposite_foregroundColorGetsReset_foregroundCollorIsNull() throws Exception {
		ExpandableComposite compositeToTest = createTestExpandableComposite(
				"ExpandableComposite { swt-titlebar-color: #FF0000; tb-toggle-color: #FF0000; tb-toggle-hover-color: #00FF00}");
		assertNotNull(compositeToTest.getTitleBarForeground());
		assertEquals(RED, compositeToTest.getTitleBarForeground().getRGB());

		css.getEngine().reset();

		assertNull(compositeToTest.getTitleBarForeground());

		ToggleHyperlink toggle = (ToggleHyperlink) compositeToTest.getChildren()[0];
		assertNull(toggle.getDecorationColor());
		assertNull(toggle.getHoverDecorationColor());
	}

}