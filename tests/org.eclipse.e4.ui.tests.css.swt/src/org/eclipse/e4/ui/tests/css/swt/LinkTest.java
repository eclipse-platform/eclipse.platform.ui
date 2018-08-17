/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 *
 * This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Sadau <lars@sadau-online.de>
 *     Fabio Zadrozny <fabiofz at gmail dot com>
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class LinkTest extends CSSSWTTestCase {

	private Link createTestLink(String styleSheet) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		Link labelToTest = new Link(panel, SWT.NONE);
		labelToTest.setText("Some text <A HREF='./somewhere'>some link text</A>");

		// Apply styles
		engine.applyStyles(labelToTest, true);

		shell.pack();
		return labelToTest;
	}

	@Test
	public void testLinkColors() {
		Link widgetToTest = createTestLink(
				"Link { background-color: #FF0000; color: #00FF00; swt-link-foreground-color: #0000FF;}");
		assertEquals(RED, widgetToTest.getBackground().getRGB());
		assertEquals(GREEN, widgetToTest.getForeground().getRGB());
		assertEquals(BLUE, widgetToTest.getLinkForeground().getRGB());
	}

}
