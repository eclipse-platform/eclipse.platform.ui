/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.e4.ui.widgets.ETabFolder;
import org.eclipse.e4.ui.widgets.ETabItem;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;

public class ETabFolderTest extends CSSSWTTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);
	static final RGB WHITE = new RGB(255, 255, 255);
	static public CSSEngine engine;
	
	protected ETabFolder createTestETabFolder(String styleSheet) {
		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		ETabFolder folderToTest = new ETabFolder(panel, SWT.NONE);
		ETabItem tab1 = new ETabItem(folderToTest, SWT.NONE);
		tab1.setText("A TAB ITEM");
		
		engine.applyStyles(shell, true);

		shell.pack();
		return folderToTest;
	}
	
	protected ToolBar[] createTestToolBars(String styleSheet) {

		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		ETabFolder folderA = new ETabFolder(panel, SWT.NONE);
		ETabItem tabA = new ETabItem(folderA, SWT.NONE);
		tabA.setText("FolderA TAB ITEM");
		ToolBar toolbarA = new ToolBar(folderA,  SWT.FLAT | SWT.HORIZONTAL);
		folderA.setTopRight(toolbarA);

		ETabFolder folderB = new ETabFolder(panel, SWT.NONE);
		ETabItem tabB = new ETabItem(folderB, SWT.NONE);
		tabB.setText("FolderB TAB ITEM");
		ToolBar toolbarB = new ToolBar(folderB,  SWT.FLAT | SWT.HORIZONTAL);
		folderB.setTopRight(toolbarB);

		//One toolbar on its own, no ETabFolder parent
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
	
	public void testBackgroundColor() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { background-color: #0000FF }");
		assertEquals(BLUE, folderToTest.getBackground().getRGB());
	}

	public void testTextColor() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { color: #0000FF }");
		assertEquals(BLUE, folderToTest.getForeground().getRGB());
	}

	//See GradientTest for testing background gradient

	public void testFontRegular() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { font: Verdana 16px }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Verdana", fontData.getName());
		assertEquals(16, fontData.getHeight());
		assertEquals(SWT.NORMAL, fontData.getStyle());		
	}

	public void testFontBold() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { font: Arial 12px; font-weight: bold }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.BOLD, fontData.getStyle());		
	}

	public void testFontItalic() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { font: Arial 12px; font-style: italic }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.ITALIC, fontData.getStyle());		
	}

	public void testBorderVisible() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { border-visible: true}");
		assertEquals(true, folderToTest.getBorderVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "border-visible", null));
		folderToTest.getShell().close();
		folderToTest = createTestETabFolder("ETabFolder { border-visible: false}");
		assertEquals(false, folderToTest.getBorderVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "border-visible", null));
	}
	
	public void testSimple() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { simple: true}");
		assertEquals(true, folderToTest.getSimple());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "simple", null));
		folderToTest.getShell().close();
		folderToTest = createTestETabFolder("ETabFolder { simple: false}");
		assertEquals(false, folderToTest.getSimple());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "simple", null));
	}
	
	public void testMaximizeVisible() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { maximize-visible: true}");
		assertEquals(true, folderToTest.getMaximizeVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "maximize-visible", null));
		folderToTest.getShell().close();
		folderToTest = createTestETabFolder("ETabFolder { maximize-visible: false}");
		assertEquals(false, folderToTest.getMaximizeVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "maximize-visible", null));
	}
	
	public void testMinimizeVisible() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { minimize-visible: true}");
		assertEquals(true, folderToTest.getMinimizeVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "minimize-visible", null));
		folderToTest.getShell().close();
		folderToTest = createTestETabFolder("ETabFolder { minimize-visible: false}");
		assertEquals(false, folderToTest.getMinimizeVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "minimize-visible", null));
	}
	
	public void testMRUVisible() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { mru-visible: true}");
		assertEquals(true, folderToTest.getMRUVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "mru-visible", null));
		folderToTest.getShell().close();
		folderToTest = createTestETabFolder("ETabFolder { mru-visible: false}");
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "mru-visible", null));
		assertEquals(false, folderToTest.getMRUVisible());
	}
	
	public void testMaximized() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { maximized: true}");
		assertEquals(true, folderToTest.getMaximized());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "maximized", null));
		folderToTest = createTestETabFolder("ETabFolder { maximized: false}");
		assertEquals(false, folderToTest.getMaximized());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "maximized", null));
	}
	
	public void testMinimized() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { minimized: true}");
		assertEquals(true, folderToTest.getMinimized());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "minimized", null));
		folderToTest = createTestETabFolder("ETabFolder { minimized: false}");
		assertEquals(false, folderToTest.getMinimized());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "minimized", null));
	}
	
	public void testTabHeight() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { tab-height: 30px }");
		assertEquals(30, folderToTest.getTabHeight());
		folderToTest = createTestETabFolder("ETabFolder { tab-height: 40px }");
		assertEquals(40, folderToTest.getTabHeight());
		
		//negative test to ensure we don't try to interpret a list
		folderToTest = createTestETabFolder("ETabFolder { tab-height: 40px 50px }");
		assertNotSame(40, folderToTest.getTabHeight());
		assertNotSame(50, folderToTest.getTabHeight());
		
		//negative test for ambiguous unit value
		folderToTest = createTestETabFolder("ETabFolder { tab-height: 40 }");
		assertNotSame(40, folderToTest.getTabHeight());

	}

	public void testSingle() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { single: true}");
		assertEquals(true, folderToTest.getSingle());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "single", null));
		folderToTest = createTestETabFolder("ETabFolder { single: false}");
		assertEquals(false, folderToTest.getSingle());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "single", null));
	}
	
	public void testUnselectedCloseVisible() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { unselected-close-visible: true}");
		assertEquals(true, folderToTest.getUnselectedCloseVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "unselected-close-visible", null));
		folderToTest = createTestETabFolder("ETabFolder { unselected-close-visible: false}");
		assertEquals(false, folderToTest.getUnselectedCloseVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "unselected-close-visible", null));
	}
	
	public void testUnselectedImageVisible() throws Exception {
		ETabFolder folderToTest = createTestETabFolder("ETabFolder { unselected-image-visible: true}");
		assertEquals(true, folderToTest.getUnselectedImageVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "unselected-image-visible", null));
		folderToTest = createTestETabFolder("ETabFolder { unselected-image-visible: false}");
		assertEquals(false, folderToTest.getUnselectedImageVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "unselected-image-visible", null));
	}
	
	public void testRetrievePropertyNull() {
		Shell shell = createShell("Shell {color:red}");
		assertEquals(null, engine.retrieveCSSProperty(shell, "border-visible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "maximized", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "maximize-visible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "minimize-visible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "mru-visible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "show-close", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "simple", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "single", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "unselected-close-visible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "unselected-image-visible", null));
	}	
	
	//TODO see bug #283585 
//	public void testTopRightAsDescendentChild() throws Exception {
//		ToolBar[] toolBars = createTestToolBars(
//				"#special ToolBar { background: #FF0000}/n" +
//				"ETabFolder ToolBar { background: #00FF00}/n" +
//				"ToolBar { background: #0000FF}");
//				
//		ToolBar barA = toolBars[0];
//		ToolBar barB = toolBars[1];
//		ToolBar barC = toolBars[2];
//		
//		SWTElement.setID(barA.getParent(), "special");
//		
//		engine.applyStyles(barA.getShell(), true);
//		assertEquals(RED, barA.getBackground().getRGB());
//		assertEquals(GREEN, barB.getBackground().getRGB());
//		assertEquals(BLUE, barC.getBackground().getRGB());
//	}
}
