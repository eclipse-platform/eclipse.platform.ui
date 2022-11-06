/*******************************************************************************
 * Copyright (c) 2014 Stefan Winkler and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Winkler <stefan@winklerweb.net> - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/

package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;

public class InnerClassElementTest extends CSSSWTTestCase {


	// create an inner class to address via CSS
	static class CustomComposite extends Composite {
		public CustomComposite(Composite parent, int style) {
			super(parent, style);
		}
	}

	@Override
	protected Label createTestLabel(String styleSheet) {
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

	@Test
	void testInnerClassElement() {
		Label label = createTestLabel("InnerClassElementTest-CustomComposite Label { color: #00ffa0; }");

		assertEquals(0x00, label.getForeground().getRed());
		assertEquals(0xff, label.getForeground().getGreen());
		assertEquals(0xa0, label.getForeground().getBlue());
	}
}
