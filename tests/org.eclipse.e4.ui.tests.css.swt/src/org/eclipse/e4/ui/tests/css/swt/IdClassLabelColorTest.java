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

import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

/*
 * Tests the CSS class and Id rules
 */

public class IdClassLabelColorTest extends CSSSWTTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);

	static final String CSS_CLASS_NAME = "makeItGreenClass";
	static final String CSS_ID = "makeItBlueID";

	@Override
	protected Label createTestLabel(String styleSheet) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());

		Label labelToTest = new Label(panel, SWT.NONE);
		labelToTest.setText("Some label text");
		WidgetElement.setCSSClass(labelToTest, CSS_CLASS_NAME);
		WidgetElement.setID(labelToTest, CSS_ID);

		// Apply styles
		engine.applyStyles(shell, true);

		shell.pack();
		return labelToTest;
	}

	//For completeness, test that the html type rule works
	@Test
	void testWidgetClass() {
		Label label = createTestLabel("Label { background-color: #FF0000 }");
		assertEquals(RED, label.getBackground().getRGB());
	}

	//Test the CSS class rule
	@Test
	void testCssClass() {
		Label labelToTest = createTestLabel("." + CSS_CLASS_NAME + " { background-color: #00FF00 }");

		//Ensure the widget actually thinks it has this CSS class
		assertEquals(CSS_CLASS_NAME, WidgetElement.getCSSClass(labelToTest));

		assertEquals(GREEN, labelToTest.getBackground().getRGB());
	}

	//Test the id rule
	@Test
	void testWidgetId() {
		Label labelToTest = createTestLabel("#" + CSS_ID + " { background-color: #0000FF }");

		//Ensure the widget actually thinks it has this ID
		assertEquals(WidgetElement.getID(labelToTest), CSS_ID);

		assertEquals(BLUE, labelToTest.getBackground().getRGB());
	}
}
