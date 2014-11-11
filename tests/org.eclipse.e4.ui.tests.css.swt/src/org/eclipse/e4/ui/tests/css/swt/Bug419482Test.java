/*******************************************************************************
 * Copyright (c) 2014 Stefan Winkler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Winkler <stefan@winklerweb.net> - initial contribution
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.junit.Test;

public class Bug419482Test extends CSSSWTTestCase {

	private static final RGB RGB_BLUE = new RGB(0, 0, 255);
	private static final RGB RGB_RED = new RGB(255, 0, 0);

	private ToolBar toolbar1;
	private ToolBar toolbar2;
	private ToolBar toolbar3;

	@Test
	public void testTwoLevelsWildcard() {
		String cssString = "Shell > * > * { color: red; } \n"
				+ "Label { color: blue; }";

		Label label = createTestLabel(cssString);

		RGB rgb = label.getForeground().getRGB();
		assertEquals(RGB_BLUE, rgb);
	}

	@Test
	public void testOneLevelWildcardOneSpecific() {
		String cssString = "Shell > * > Label { color: red; } \n"
				+ "Label { color: blue; }";

		Label label = createTestLabel(cssString);

		RGB rgb = label.getForeground().getRGB();
		assertEquals(RGB_RED, rgb);
	}

	@Test
	public void testDescendentsWildcard() {
		String cssString = "Shell * { color: red; } \n"
				+ "Label { color: blue; }";

		Label label = createTestLabel(cssString);

		RGB rgb = label.getForeground().getRGB();
		assertEquals(RGB_BLUE, rgb);
	}

	@Test
	public void testDescendentsSpecific() {
		String cssString = "Shell Label { color: red; } \n"
				+ "Label { color: blue; }";

		Label label = createTestLabel(cssString);

		RGB rgb = label.getForeground().getRGB();
		assertEquals(RGB_RED, rgb);
	}




	@Test
	public void testOriginalBugReport() {
		String css = "Shell, Shell > *, Shell > * > * {\n" +
				"    background-color: red;\n" +
				"}\n" +
				"ToolBar {\n" +
				"    background-color: blue;\n" +
				"}";

		engine = createEngine(css, display);

		Shell shell = createShellWithToolbars(display);

		// Apply styles
		engine.applyStyles(shell, true);

		assertEquals(RGB_BLUE, toolbar1.getBackground().getRGB());
		assertEquals(RGB_BLUE, toolbar2.getBackground().getRGB());
		assertEquals(RGB_BLUE, toolbar3.getBackground().getRGB());
	}

	@Test
	public void testOriginalBugReportDifferentOrder() {
		String css = "ToolBar {\n" + "    background-color: blue;\n" + "}"
				+ "Shell, Shell > *, Shell > * > * {\n"
				+ "    background-color: red;\n" + "}\n";

		engine = createEngine(css, display);

		// Create widgets
		Shell shell = createShellWithToolbars(display);

		// Apply styles
		engine.applyStyles(shell, true);

		assertEquals(RGB_RED, toolbar1.getBackground().getRGB());
		assertEquals(RGB_RED, toolbar2.getBackground().getRGB());
		assertEquals(RGB_BLUE, toolbar3.getBackground().getRGB());
	}

	private Shell createShellWithToolbars(Display display) {
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setLayout(new RowLayout(SWT.VERTICAL));

		toolbar1 = new ToolBar(shell, SWT.BORDER);
		Composite composite1 = new Composite(shell, SWT.NONE);
		composite1.setLayout(new RowLayout(SWT.VERTICAL));

		toolbar2 = new ToolBar(composite1, SWT.BORDER);
		Composite composite2 = new Composite(composite1, SWT.NONE);
		composite2.setLayout(new RowLayout(SWT.VERTICAL));

		toolbar3 = new ToolBar(composite2, SWT.BORDER);
		return shell;

	}

}
