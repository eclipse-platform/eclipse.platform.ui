/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.HashSet;

import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

public class ShellTest extends CSSSWTTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);

	protected Shell createTestShell(String styleSheet) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		// Apply styles
		engine.applyStyles(shell, true);

		shell.pack();
		return shell;
	}


	@Test
	void testColor() {
		Shell shellToTest = createTestShell("Shell { background-color: #FF0000; color: #0000FF }");
		assertEquals(RED, shellToTest.getBackground().getRGB());
		assertEquals(BLUE, shellToTest.getForeground().getRGB());
	}

	@Test
	void testFontRegular() {
		Shell shellToTest = createTestShell("Shell { font: Verdana 16px }");
		assertEquals(1, shellToTest.getFont().getFontData().length);
		FontData fontData = shellToTest.getFont().getFontData()[0];
		assertEquals("Verdana", fontData.getName());
		assertEquals(16, fontData.getHeight());
		assertEquals(SWT.NORMAL, fontData.getStyle());
	}

	@Test
	void testFontBold() {
		Shell shellToTest = createTestShell("Shell { font: Arial 12px; font-weight: bold }");
		assertEquals(1, shellToTest.getFont().getFontData().length);
		FontData fontData = shellToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.BOLD, fontData.getStyle());
	}

	@Test
	void testFontItalic() {
		Shell shellToTest = createTestShell("Shell { font-style: italic }");
		assertEquals(1, shellToTest.getFont().getFontData().length);
		FontData fontData = shellToTest.getFont().getFontData()[0];
		assertEquals(SWT.ITALIC, fontData.getStyle());
	}

	// bug 375069: ensure children aren't caught up in parent
	@Test
	void test375069ChildShellDifferentiation() {
		engine = createEngine("Shell.parent { font-style: italic; }", display);

		Shell parent = new Shell(display, SWT.NONE);
		WidgetElement.setCSSClass(parent, "parent");
		Shell child = new Shell(parent, SWT.NONE);
		WidgetElement.setCSSClass(child, "child");
		parent.open();
		child.open();
		engine.applyStyles(parent, true);
		engine.applyStyles(child, true);


		assertEquals(1, parent.getFont().getFontData().length);
		FontData fontData = parent.getFont().getFontData()[0];
		assertEquals(SWT.ITALIC, fontData.getStyle());

		assertEquals(1, child.getFont().getFontData().length);
		fontData = child.getFont().getFontData()[0];
		assertNotSame(SWT.ITALIC, fontData.getStyle());
	}

	// bug 375069: ensure children shells are still captured by Shell
	@Test
	void test375069AllShell() {
		engine = createEngine("Shell { font-style: italic; }", display);

		Shell parent = new Shell(display, SWT.NONE);
		WidgetElement.setCSSClass(parent, "parent");
		Shell child = new Shell(parent, SWT.NONE);
		WidgetElement.setCSSClass(child, "child");
		parent.open();
		child.open();
		engine.applyStyles(parent, true);
		engine.applyStyles(child, true);

		assertEquals(1, parent.getFont().getFontData().length);
		FontData fontData = parent.getFont().getFontData()[0];
		assertEquals(SWT.ITALIC, fontData.getStyle());

		assertEquals(1, child.getFont().getFontData().length);
		fontData = child.getFont().getFontData()[0];
		assertEquals(SWT.ITALIC, fontData.getStyle());
	}

	// bug 375069: ensure children shells are still captured by Shell
	@Test
	void testShellParentage() {
		engine = createEngine(
				"Shell[parentage='parent'] { font-style: italic; }", display);

		Shell parent = new Shell(display, SWT.NONE);
		WidgetElement.setID(parent, "parent");
		Shell child = new Shell(parent, SWT.NONE);
		WidgetElement.setID(child, "child");
		parent.open();
		child.open();
		engine.applyStyles(parent, true);
		engine.applyStyles(child, true);

		assertEquals(1, parent.getFont().getFontData().length);
		FontData fontData = parent.getFont().getFontData()[0];
		assertNotSame(SWT.ITALIC, fontData.getStyle());

		assertEquals(1, child.getFont().getFontData().length);
		fontData = child.getFont().getFontData()[0];
		assertEquals(SWT.ITALIC, fontData.getStyle());
	}

	@Test
	void testShellUnparentedPseudoelement() {
		engine = createEngine(
				"Shell:swt-unparented { font-style: italic; }", display);

		Shell parent = new Shell(display, SWT.NONE);
		WidgetElement.setCSSClass(parent, "parent");
		Shell child = new Shell(parent, SWT.NONE);
		WidgetElement.setCSSClass(child, "child");
		parent.open();
		child.open();
		engine.applyStyles(parent, true);
		engine.applyStyles(child, true);

		assertEquals(1, parent.getFont().getFontData().length);
		FontData fontData = parent.getFont().getFontData()[0];
		assertEquals(SWT.ITALIC, fontData.getStyle());

		assertEquals(1, child.getFont().getFontData().length);
		fontData = child.getFont().getFontData()[0];
		assertNotSame(SWT.ITALIC, fontData.getStyle());
	}

	@Test
	void testShellParentedPseudoelement() {
		engine = createEngine(
				"Shell:swt-parented { font-style: italic; }", display);

		Shell parent = new Shell(display, SWT.NONE);
		WidgetElement.setCSSClass(parent, "parent");
		Shell child = new Shell(parent, SWT.NONE);
		WidgetElement.setCSSClass(child, "child");
		parent.open();
		child.open();
		engine.applyStyles(parent, true);
		engine.applyStyles(child, true);

		assertEquals(1, parent.getFont().getFontData().length);
		FontData fontData = parent.getFont().getFontData()[0];
		assertNotSame(SWT.ITALIC, fontData.getStyle());

		assertEquals(1, child.getFont().getFontData().length);
		fontData = child.getFont().getFontData()[0];
		assertEquals(SWT.ITALIC, fontData.getStyle());
	}

	@Test
	void testSwtDataClassAttribute() {
		engine = createEngine(
				"Shell[swt-data-class ~= 'java.util.HashSet'] { font-style: italic; }",
				display);

		Shell parent = new Shell(display, SWT.NONE);
		parent.setData(new HashSet<>());
		parent.open();
		engine.applyStyles(parent, true);

		assertEquals(1, parent.getFont().getFontData().length);
		FontData fontData = parent.getFont().getFontData()[0];
		assertEquals(SWT.ITALIC, fontData.getStyle());
	}

	@Test
	void testBackgroundMode() {
		Shell shellToTest = createTestShell("Shell { swt-background-mode: force; }");
		assertEquals(SWT.INHERIT_FORCE, shellToTest.getBackgroundMode());
	}

}