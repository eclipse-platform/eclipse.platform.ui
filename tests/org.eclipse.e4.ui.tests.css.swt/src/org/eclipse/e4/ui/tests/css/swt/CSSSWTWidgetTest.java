/*******************************************************************************
 *  Copyright (c) 2009, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

public class CSSSWTWidgetTest extends CSSSWTTestCase {

	CSSEngine engine;
	
	protected Widget createTestLabel(String styleSheet) {
		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);	
		
		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Label labelToTest = new Label(shell, SWT.NONE);
		labelToTest.setText("Some label text");

		// Apply styles
		engine.applyStyles(labelToTest, true);

		return labelToTest;
	}
	
//	public void testEngineKey()  throws Exception {
//		Widget widget = createTestLabel("Label { font: Arial 12px; font-weight: bold }");
//		assertEquals(WidgetElement.getEngine(widget), engine);		
//	}

	public void testIDKey()  throws Exception {
		final String id = "some.test.id";
		Widget widget = createTestLabel("Label { font: Arial 12px; font-weight: bold }");
		WidgetElement.setID(widget, id);
		assertEquals(WidgetElement.getID(widget), id);	
	}


	public void testCSSClassKey()  throws Exception {
		final String cssClass = "some.test.cssclassname";
		Widget widget = createTestLabel("Label { font: Arial 12px; font-weight: bold }");
		WidgetElement.setCSSClass(widget, cssClass);
		assertEquals(WidgetElement.getCSSClass(widget), cssClass);	
	}
}
