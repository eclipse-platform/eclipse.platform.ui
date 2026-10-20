/*******************************************************************************
 * Copyright (c) 2019 Airbus Defence and Space GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Airbus Defence and Space GmbH - initial API and implementation
 *     Benedikt Kuntz <benedikt.kuntz@airbus.com>
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
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.ToggleHyperlink;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SectionTest {

	@RegisterExtension
	CssSwtEngine css = new CssSwtEngine();

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);

	protected Section createTestSection(String styleSheet) {
		Display display = css.getDisplay();
		CSSEngine engine = css.createEngine(styleSheet);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite compositeToTest = new Composite(shell, SWT.NONE);
		compositeToTest.setLayout(new FillLayout());

		Section test = new Section(shell, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);

		// Apply styles
		engine.applyStyles(shell, true);

		return test;
	}

	@Test
	void testSectionColors() {
		Section section = createTestSection(
				"""
					Section { swt-titlebar-color: #FF0000;\
					tb-toggle-color: #FF0000; \
					tb-toggle-hover-color: #00FF00; \
					background-color-gradient-titlebar: #00FF00; \
					background-color-titlebar: #0000FF; \
					border-color-titlebar: #00FF00}""");
		assertNotNull(section.getTitleBarForeground());
		assertEquals(RED, section.getTitleBarForeground().getRGB());
		assertNotNull(section.getTitleBarBackground());
		assertEquals(BLUE, section.getTitleBarBackground().getRGB());
		assertNotNull(section.getTitleBarGradientBackground());
		assertEquals(GREEN, section.getTitleBarGradientBackground().getRGB());
		assertNotNull(section.getTitleBarBorderColor());
		assertEquals(GREEN, section.getTitleBarBorderColor().getRGB());

		ToggleHyperlink toggle = (ToggleHyperlink) section.getChildren()[0];
		assertNotNull(toggle.getDecorationColor());
		assertEquals(RED, toggle.getDecorationColor().getRGB());
		assertNotNull(toggle.getHoverDecorationColor());
		assertEquals(GREEN, toggle.getHoverDecorationColor().getRGB());
	}

	@Test
	void testSectionResetColors() throws Exception {
		Section section = createTestSection(
				"""
					Section { swt-titlebar-color: #FF0000;\
					tb-toggle-color: #FF0000; \
					tb-toggle-hover-color: #00FF00; \
					background-color-gradient-titlebar: #00FF00; \
					background-color-titlebar: #0000FF; \
					border-color-titlebar: #00FF00}""");

		css.getEngine().reset();

		assertNull(section.getTitleBarForeground());

		ToggleHyperlink toggle = (ToggleHyperlink) section.getChildren()[0];
		assertNull(toggle.getDecorationColor());
		assertNull(toggle.getHoverDecorationColor());

	}

}