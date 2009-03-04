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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CTabFolderTest extends CSSTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);
	static final RGB WHITE = new RGB(255, 255, 255);
	static public CSSEngine engine;
	
	protected CTabFolder createTestCTabFolder(String styleSheet) {
		Display display = Display.getDefault();
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
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { background-color: #0000FF }");
		assertEquals(BLUE, folderToTest.getBackground().getRGB());
	}

	public void testTextColor() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { color: #0000FF }");
		assertEquals(BLUE, folderToTest.getForeground().getRGB());
	}

//	public void testGradientColor() throws Exception {
//		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { background-color: #FF0000  #0000FF }");
//		assertEquals(BLUE, folderToTest.getSelectionBackground());
//		folderToTest.getShell().close();
//	}

	public void testSelectedPseudo() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder(
				"CTabFolder { color: #FFFFFF; background-color: #0000FF }\n" +
				"CTabFolder:selected { color: #FF0000;  background-color: #00FF00 }");
		assertEquals(WHITE, folderToTest.getForeground().getRGB());
		assertEquals(BLUE, folderToTest.getBackground().getRGB());
		assertEquals(RED, folderToTest.getSelectionForeground().getRGB());
		assertEquals(GREEN, folderToTest.getSelectionBackground().getRGB());
	}
		
	public void testFontRegular() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { font: Verdana 16px }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Verdana", fontData.getName());
		assertEquals(16, fontData.getHeight());
		assertEquals(SWT.NORMAL, fontData.getStyle());		
	}

	public void testFontBold() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { font: Arial 12px; font-weight: bold }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.BOLD, fontData.getStyle());		
	}

	public void testFontItalic() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { font: Arial 12px; font-style: italic }");
		assertEquals(1, folderToTest.getFont().getFontData().length);
		FontData fontData = folderToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.ITALIC, fontData.getStyle());		
	}

	public void testBorderVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { borderVisible: true}");
		assertEquals(true, folderToTest.getBorderVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "borderVisible", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { borderVisible: false}");
		assertEquals(false, folderToTest.getBorderVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "borderVisible", null));
	}
	
	public void testSimple() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { simple: true}");
		assertEquals(true, folderToTest.getSimple());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "simple", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { simple: false}");
		assertEquals(false, folderToTest.getSimple());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "simple", null));
	}
	
	public void testMaximizeVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { maximizeVisible: true}");
		assertEquals(true, folderToTest.getMaximizeVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "maximizeVisible", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { maximizeVisible: false}");
		assertEquals(false, folderToTest.getMaximizeVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "maximizeVisible", null));
	}
	
	public void testMinimizeVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { minimizeVisible: true}");
		assertEquals(true, folderToTest.getMinimizeVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "minimizeVisible", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { minimizeVisible: false}");
		assertEquals(false, folderToTest.getMinimizeVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "minimizeVisible", null));
	}
	
	public void testMRUVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { mruVisible: true}");
		assertEquals(true, folderToTest.getMRUVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "mruVisible", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { mruVisible: false}");
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "mruVisible", null));
		assertEquals(false, folderToTest.getMRUVisible());
	}
	
	public void testMaximized() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { maximized: true}");
		assertEquals(true, folderToTest.getMaximized());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "maximized", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { maximized: false}");
		assertEquals(false, folderToTest.getMaximized());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "maximized", null));
	}
	
	public void testMinimized() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { minimized: true}");
		assertEquals(true, folderToTest.getMinimized());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "minimized", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { minimized: false}");
		assertEquals(false, folderToTest.getMinimized());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "minimized", null));
	}
	
	public void testSingle() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { single: true}");
		assertEquals(true, folderToTest.getSingle());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "single", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { single: false}");
		assertEquals(false, folderToTest.getSingle());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "single", null));
	}
	
	public void testUnselectedCloseVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { unselectedCloseVisible: true}");
		assertEquals(true, folderToTest.getUnselectedCloseVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "unselectedCloseVisible", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { unselectedCloseVisible: false}");
		assertEquals(false, folderToTest.getUnselectedCloseVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "unselectedCloseVisible", null));
	}
	
	public void testUnselectedImageVisible() throws Exception {
		CTabFolder folderToTest = createTestCTabFolder("CTabFolder { unselectedImageVisible: true}");
		assertEquals(true, folderToTest.getUnselectedImageVisible());
		assertEquals("true", engine.retrieveCSSProperty(folderToTest, "unselectedImageVisible", null));
		folderToTest.getShell().close();
		folderToTest = createTestCTabFolder("CTabFolder { unselectedImageVisible: false}");
		assertEquals(false, folderToTest.getUnselectedImageVisible());
		assertEquals("false", engine.retrieveCSSProperty(folderToTest, "unselectedImageVisible", null));
	}
	
	public void testRetrievePropertyNull() {
		Shell shell = createShell("Shell {color:red}");
		assertEquals(null, engine.retrieveCSSProperty(shell, "borderVisible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "maximized", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "maximizeVisible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "minimizeVisible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "mruVisible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "showClose", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "simple", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "single", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "unselectedCloseVisible", null));
		assertEquals(null, engine.retrieveCSSProperty(shell, "unselectedImageVisible", null));
	}
}
