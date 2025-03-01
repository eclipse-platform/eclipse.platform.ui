/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
 *     Andrey Loskutov <loskutov@gmx.de> - Bug 388476
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.junit.jupiter.api.Test;

public class CTabFolderTest extends CSSSWTTestCase {

	protected CTabFolder createTestCTabFolder(String styleSheet) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		CTabFolder folderToTest = new CTabFolder(panel, SWT.NONE);
		CTabItem tab1 = new CTabItem(folderToTest, SWT.NONE);
		tab1.setText("A TAB ITEM");

		engine.applyStyles(shell, true);

		shell.pack();
		return folderToTest;
	}

	protected ToolBar[] createTestToolBars(String styleSheet) {

		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		CTabFolder folderA = new CTabFolder(panel, SWT.NONE);
		CTabItem tabA = new CTabItem(folderA, SWT.NONE);
		tabA.setText("FolderA TAB ITEM");
		ToolBar toolbarA = new ToolBar(folderA,  SWT.FLAT | SWT.HORIZONTAL);
		folderA.setTopRight(toolbarA);

		CTabFolder folderB = new CTabFolder(panel, SWT.NONE);
		CTabItem tabB = new CTabItem(folderB, SWT.NONE);
		tabB.setText("FolderB TAB ITEM");
		ToolBar toolbarB = new ToolBar(folderB,  SWT.FLAT | SWT.HORIZONTAL);
		folderB.setTopRight(toolbarB);

		//One toolbar on its own, no CTabFolder parent
		ToolBar toolbarC = new ToolBar(panel,  SWT.FLAT | SWT.HORIZONTAL);

		engine.applyStyles(shell, true);
		return new ToolBar[] {toolbarA, toolbarB, toolbarC};
	}

	protected Shell createShell(String styleSheet) {
		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.NONE);

		engine.applyStyles(shell, true);

		shell.pack();
		return shell;
	}

	protected Label createLabelInCTabFolder(String styleSheet) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		CTabFolder folderToTest = new CTabFolder(panel, SWT.NONE);
		CTabItem tab1 = new CTabItem(folderToTest, SWT.NONE);
		tab1.setText("A TAB ITEM");
		Composite composite = new Composite(folderToTest, SWT.BORDER);
		composite.setLayout(new FillLayout());
		Label labelToTest = new Label(composite, SWT.BORDER);
		labelToTest.setText("Text for item ");
		tab1.setControl(composite);
		// setSelection is required to process via the CSS engine the children of
		// CTabItem
		folderToTest.setSelection(0);

		engine.applyStyles(shell, true);

		shell.pack();
		return labelToTest;
	}

	@Test
	void testBackgroundColor() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { background-color: #0000FF }");
		assertEquals(BLUE, folderToTest.getBackground().getRGB());
	}

	@Test
	void testTextColor() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { color: #0000FF }");
		assertEquals(BLUE, folderToTest.getForeground().getRGB());
	}

	//See GradientTest for testing background gradient

	@Test
	void testFontRegular() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { font: Verdana 16px }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Verdana", fontData.getName());
		assertEquals(16, fontData.getHeight());
		assertEquals(SWT.NORMAL, fontData.getStyle());
	}

	@Test
	void testFontBold() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { font: Arial 12px; font-weight: bold }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.BOLD, fontData.getStyle());
	}

	@Test
	void testFontItalic() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { font: Arial 12px; font-style: italic }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.ITALIC, fontData.getStyle());
	}

	@Test
	void testBorderVisible() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { border-visible: true}");
		assertEquals(true, folderToTest.getBorderVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "border-visible", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { border-visible: false}");
		assertEquals(false, folderToTest.getBorderVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "border-visible", null));
	}
	@Test
	void testSimple() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { swt-simple: true}");
		assertEquals(true, folderToTest.getSimple());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "swt-simple", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { swt-simple: false}");
		assertEquals(false, folderToTest.getSimple());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "swt-simple", null));
	}

	@Test
	void testMaximizeVisible() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { swt-maximize-visible: true}");
		assertEquals(true, folderToTest.getMaximizeVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "swt-maximize-visible", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { swt-maximize-visible: false}");
		assertEquals(false, folderToTest.getMaximizeVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "swt-maximize-visible", null));
	}

	@Test
	void testMinimizeVisible() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { swt-minimize-visible: true}");
		assertEquals(true, folderToTest.getMinimizeVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "swt-minimize-visible", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { swt-minimize-visible: false}");
		assertEquals(false, folderToTest.getMinimizeVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "swt-minimize-visible", null));
	}

	@Test
	void testMaximized() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { swt-maximized: true}");
		assertEquals(true, folderToTest.getMaximized());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "swt-maximized", null));
		folderToTest = createTestCTabFolder("CTabFolder { swt-maximized: false}");
		assertEquals(false, folderToTest.getMaximized());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "swt-maximized", null));
	}

	@Test
	void testMinimized() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { swt-minimized: true}");
		assertEquals(true, folderToTest.getMinimized());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "swt-minimized", null));
		folderToTest = createTestCTabFolder("CTabFolder { swt-minimized: false}");
		assertEquals(false, folderToTest.getMinimized());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "swt-minimized", null));
	}

	@Test
	void testTabHeight() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { swt-tab-height: 30px }");
		assertEquals(30, folderToTest.getTabHeight());
		folderToTest = createTestCTabFolder("CTabFolder { swt-tab-height: 40px }");
		assertEquals(40, folderToTest.getTabHeight());

		//negative test to ensure we don't try to interpret a list
		folderToTest = createTestCTabFolder("CTabFolder { swt-tab-height: 40px 50px }");
		assertNotSame(40, folderToTest.getTabHeight());
		assertNotSame(50, folderToTest.getTabHeight());

		//negative test for ambiguous unit value
		folderToTest = createTestCTabFolder("CTabFolder { swt-tab-height: 40 }");
		assertNotSame(40, folderToTest.getTabHeight());

	}

	@Test
	void testSingle() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { swt-single: true}");
		assertEquals(true, folderToTest.getSingle());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "swt-single", null));
		folderToTest = createTestCTabFolder("CTabFolder { swt-single: false}");
		assertEquals(false, folderToTest.getSingle());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "swt-single", null));
	}

	@Test
	void testUnselectedCloseVisible() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { swt-unselected-close-visible true}");
		assertEquals(true, folderToTest.getUnselectedCloseVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "swt-unselected-close-visible", null));
		folderToTest = createTestCTabFolder("CTabFolder { swt-unselected-close-visible: false}");
		assertEquals(false, folderToTest.getUnselectedCloseVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "swt-unselected-close-visible", null));
	}

	@Test
	void testUnselectedImageVisible() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { swt-unselected-image-visible: true}");
		assertEquals(true, folderToTest.getUnselectedImageVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "swt-unselected-image-visible", null));
		folderToTest = createTestCTabFolder("CTabFolder { swt-unselected-image-visible: false}");
		assertEquals(false, folderToTest.getUnselectedImageVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "swt-unselected-image-visible", null));
	}

	@Test
	void testRetrievePropertyNull() {
		Shell shell = createShell("Shell {color:red}");
		assertEquals(null, engine.retrieveCSSProperty(shell, "border-visible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "swt-maximized", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "swt-maximize-visible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "swt-minimize-visible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "swt-simple", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "swt-single", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "swt-unselected-close-visible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "swt-unselected-image-visible", null));
	}

	@Test
	void testTopRightAsDescendentChild() {
		ToolBar[] toolBars = createTestToolBars(
				"""
					CTabFolder.special ToolBar { background: #FF0000}
					CTabFolder ToolBar { background: #00FF00}
					CTabFolder.extraordinary ToolBar { background: #FFFFFF}
					ToolBar { background: #0000FF}""");

		ToolBar barA = toolBars[0];
		ToolBar barB = toolBars[1];
		ToolBar barC = toolBars[2];

		WidgetElement.setCSSClass(barA.getParent(), "special");
		engine.applyStyles(barA.getShell(), true);

		assertEquals(RED, barA.getBackground().getRGB());
		assertEquals(GREEN, barB.getBackground().getRGB());
		assertEquals(BLUE, barC.getBackground().getRGB());

		WidgetElement.setCSSClass(barA.getParent(), "extraordinary");
		engine.applyStyles(barA.getShell(), true);

		assertEquals(WHITE, barA.getBackground().getRGB());
	}

	@Test
	void testStyleLabelChildInCTabFolder() {
		Label labelToTest = createLabelInCTabFolder("Label { background-color: #0000FF; }\n");
		assertEquals(BLUE, labelToTest.getBackground().getRGB());
	}

	@Test
	void testSelectedImageVisible() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { swt-selected-image-visible: true}");
		assertEquals(true, folderToTest.getSelectedImageVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "swt-selected-image-visible", null));
		folderToTest = createTestCTabFolder("CTabFolder { swt-selected-image-visible: false}");
		assertEquals(false, folderToTest.getSelectedImageVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "swt-selected-image-visible", null));
	}

	@Test
	void testMinimumCharacters() {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { swt-tab-text-minimum-characters: 1}");
		assertEquals(1, folderToTest.getMinimumCharacters());
		assertEquals("1", engine.retrieveCSSProperty(folderToTest, "swt-tab-text-minimum-characters", null));
		folderToTest = createTestCTabFolder("CTabFolder { swt-tab-text-minimum-characters: 1.2}");
		assertEquals(1, folderToTest.getMinimumCharacters());
		assertEquals("1", engine.retrieveCSSProperty(folderToTest, "swt-tab-text-minimum-characters", null));
	}
}
