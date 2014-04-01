/*******************************************************************************
 * Copyright (c) 2014 Stefan Winkler and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Winkler <stefan@winklerweb.net> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class InnerClassElementTest extends CSSSWTTestCase {

	CSSEngine engine;

	// create an inner class to address via CSS
	class CustomComposite extends Composite {
		public CustomComposite(Composite parent, int style) {
			super(parent, style);
		}
	}

	protected Label createTestLabel(String styleSheet) {
		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		CustomComposite composite = new CustomComposite(shell, SWT.NONE);
		composite.setLayout(new FillLayout());

		Label labelToTest = new Label(composite, SWT.NONE);
		labelToTest.setText("Some label text");

		// Apply styles
		engine.applyStyles(labelToTest, true);

		return labelToTest;
	}

	public void testInnerClassElement() throws Exception {
		Label label = createTestLabel("InnerClassElementTest-CustomComposite Label { color: #00ffa0; }");

		assertEquals(0x00, label.getForeground().getRed());
		assertEquals(0xff, label.getForeground().getGreen());
		assertEquals(0xa0, label.getForeground().getBlue());
	}
}
