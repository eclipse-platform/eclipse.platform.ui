/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *      Remy Chi Jian Suen <remy.suen@gmail.com> - bug 137650
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ButtonTest extends CSSSWTTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);
	CSSEngine engine;	
	
	protected Button createTestButton(String styleSheet) {
		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);
		
		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		Button buttonToTest = new Button(panel, SWT.CHECK);
		buttonToTest.setText("Some button text");

		// Apply styles
		engine.applyStyles(shell, true);

		shell.pack();
		return buttonToTest;
	}
	
	protected Button createTestArrowButton(String styleSheet) {
		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);
		
		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		Button buttonToTest = new Button(panel, SWT.ARROW);

		// Apply styles
		engine.applyStyles(shell, true);

		shell.pack();
		return buttonToTest;
	}
	
	public void testColor() throws Exception {
		Button buttonToTest = createTestButton("Button { background-color: #FF0000; color: #0000FF }");
		assertEquals(RED, buttonToTest.getBackground().getRGB());
		assertEquals(BLUE, buttonToTest.getForeground().getRGB());
	}

	public void testFontRegular() throws Exception {
		Button buttonToTest = createTestButton("Button { font: Verdana 16px }");
		assertEquals(1, buttonToTest.getFont().getFontData().length);
		FontData fontData = buttonToTest.getFont().getFontData()[0];
		assertEquals("Verdana", fontData.getName());
		assertEquals(16, fontData.getHeight());
		assertEquals(SWT.NORMAL, fontData.getStyle());		
	}

	public void testFontBold() throws Exception {
		Button buttonToTest = createTestButton("Button { font: Arial 12px; font-weight: bold }");
		assertEquals(1, buttonToTest.getFont().getFontData().length);
		FontData fontData = buttonToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.BOLD, fontData.getStyle());		
	}

	public void testFontItalic() throws Exception {
		Button buttonToTest = createTestButton("Button { font-style: italic }");
		assertEquals(1, buttonToTest.getFont().getFontData().length);
		FontData fontData = buttonToTest.getFont().getFontData()[0];
		assertEquals(SWT.ITALIC, fontData.getStyle());		
	}
	
//	public void testSelectedPseudo() throws Exception {
//		Button buttonToTest = createTestButton("Button { color: #FF0000; }\n" + 
//				"Button:selected { color: #0000FF; }");
//		assertEquals(RED, buttonToTest.getForeground().getRGB());
//		buttonToTest.setSelection(true);
//		engine.applyStyles(buttonToTest.getShell(), true);
//		assertEquals(BLUE, buttonToTest.getForeground().getRGB());
//	}
	
	public void testAlignment() throws Exception {
		Button buttonToTest = createTestButton("Button { alignment: right; }");		
		assertEquals(SWT.RIGHT, buttonToTest.getAlignment());
		
		buttonToTest = createTestButton("Button { alignment: left; }");
		assertEquals(SWT.LEFT, buttonToTest.getAlignment());
		
		buttonToTest = createTestButton("Button { alignment: center; }");
		assertEquals(SWT.CENTER, buttonToTest.getAlignment());
	}
	
	public void testAlignment2() throws Exception {
		Button buttonToTest = createTestButton("Button { alignment: trail; }");
		assertEquals(SWT.TRAIL, buttonToTest.getAlignment());
		
		buttonToTest = createTestButton("Button { alignment: lead; }");
		assertEquals(SWT.LEAD, buttonToTest.getAlignment());
	}

	public void testArrowAlignment() throws Exception {
		Button buttonToTest = createTestArrowButton("Button { alignment: up; }");		
		assertEquals(SWT.UP, buttonToTest.getAlignment());
		
		buttonToTest = createTestArrowButton("Button { alignment: down; }");
		assertEquals(SWT.DOWN, buttonToTest.getAlignment());
		
		buttonToTest = createTestArrowButton("Button { alignment: left; }");
		assertEquals(SWT.LEFT, buttonToTest.getAlignment());
		
		buttonToTest = createTestArrowButton("Button { alignment: right; }");
		assertEquals(SWT.RIGHT, buttonToTest.getAlignment());
	}

}
