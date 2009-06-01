/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others. All rights reserved. This
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class LabelTest extends CSSSWTTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);
	
	CSSEngine engine;
		
	protected Label createTestLabel(String styleSheet) {
		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);
		
		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		Label labelToTest = new Label(panel, SWT.NONE);
		labelToTest.setText("Some label text");

		// Apply styles
		engine.applyStyles(labelToTest, true);

		shell.pack();
		return labelToTest;
	}
	
	
	public void testColor() throws Exception {
		Label labelToTest = createTestLabel("Label { background-color: #FF0000; color: #0000FF }");
		assertEquals(RED, labelToTest.getBackground().getRGB());
		assertEquals(BLUE, labelToTest.getForeground().getRGB());
	}

	public void testFontRegular() throws Exception {
		Label labelToTest = createTestLabel("Label { font: Verdana 16px }");
		assertEquals(1, labelToTest.getFont().getFontData().length);
		FontData fontData = labelToTest.getFont().getFontData()[0];
		assertEquals("Verdana", fontData.getName());
		assertEquals(16, fontData.getHeight());
		assertEquals(SWT.NORMAL, fontData.getStyle());		
	}

	public void testFontBold() throws Exception {
		Label labelToTest = createTestLabel("Label { font: Arial 12px; font-weight: bold }");
		assertEquals(1, labelToTest.getFont().getFontData().length);
		FontData fontData = labelToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.BOLD, fontData.getStyle());		
	}

	public void testFontItalic() throws Exception {
		Label labelToTest = createTestLabel("Label { font-style: italic }");
		assertEquals(1, labelToTest.getFont().getFontData().length);
		FontData fontData = labelToTest.getFont().getFontData()[0];
		assertEquals(SWT.ITALIC, fontData.getStyle());		
	}
	
	public void testAlignment() throws Exception {
		Label labelToTest = createTestLabel("Label { alignment: right }");
		assertEquals(SWT.RIGHT, labelToTest.getAlignment());
		
		labelToTest = createTestLabel("Label { alignment: center; }");
		assertEquals(SWT.CENTER, labelToTest.getAlignment());

		labelToTest = createTestLabel("Label { alignment: left; }");
		assertEquals(SWT.LEFT, labelToTest.getAlignment());
		
	}
	
	public void testAlignment2() throws Exception {
		Label labelToTest = createTestLabel("Label { alignment: trail }");
		assertEquals(SWT.TRAIL, labelToTest.getAlignment());
		
		labelToTest = createTestLabel("Label { alignment: lead; }");
		assertEquals(SWT.LEAD, labelToTest.getAlignment());
	}
}
