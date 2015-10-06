/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.junit.After;
import org.junit.Before;

public class CSSSWTTestCase {

	protected Display display;
	protected CSSEngine engine;

	public CSSEngine createEngine(String styleSheet, Display display) {
		engine = new CSSSWTEngineImpl(display);

		engine.setErrorHandler(e -> fail(e.getMessage()));

		try {
			engine.parseStyleSheet(new StringReader(styleSheet));
		} catch (IOException e) {
			fail(e.getMessage());
		}
		return engine;

	}

	/**
	 * Parse and apply the style sheet, forgetting previous style sheets applied.
	 * This is helpful for reusing the same engine but writing independent tests.
	 * Styles are applied down the widget hierarchy.
	 * @param engine the engine
	 * @param widget the start of the widget hierarchy
	 * @param styleSheet a string style sheet
	 */
	public void clearAndApply(CSSEngine engine, Widget widget, String styleSheet) {

		//Forget all previous styles
		engine.reset();
		if (styleSheet != null) {

			try {
				engine.parseStyleSheet(new StringReader(styleSheet));
			} catch (IOException e) {
				fail(e.getMessage());
			}
		}

		engine.applyStyles(widget, true, true);
	}

	@Before
	public void setUp() {
		display = Display.getDefault();
	}

	@After
	public void tearDown() {
		display = Display.getDefault();
		if (!display.isDisposed()) {
			for (Shell shell : display.getShells()) {
				shell.dispose();
			}
			display.dispose();
		}
	}

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

		// Apply styles
		engine.applyStyles(labelToTest, true);

		shell.pack();
		return labelToTest;
	}

}
