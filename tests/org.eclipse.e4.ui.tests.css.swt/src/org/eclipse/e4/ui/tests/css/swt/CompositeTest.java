/*******************************************************************************
 * Copyright (c) 2017 vogella GmbH and others.
 *
 * This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 	Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

public class CompositeTest extends CSSSWTTestCase {


	protected Composite createTestComposite(String styleSheet) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite compositeToTest = new Composite(shell, SWT.NONE);
		compositeToTest.setLayout(new FillLayout());

		// Apply styles
		engine.applyStyles(compositeToTest, true);

		shell.pack();
		return compositeToTest;
	}

	protected Composite createTestCompositeAsInnerClass(String styleSheet) {
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite compositeToTest = new Composite(shell, SWT.NONE) {
		};

		compositeToTest.setLayout(new FillLayout());

		// Apply styles
		engine.applyStyles(compositeToTest, true);

		shell.pack();
		return compositeToTest;
	}

	@Test
	public void testCompositeColor() {
		Composite compositeToTest = createTestComposite("Composite { background-color: #FF0000; color: #0000FF }");
		assertEquals(RED, compositeToTest.getBackground().getRGB());
		assertEquals(BLUE, compositeToTest.getForeground().getRGB());
	}

	@Test

	public void testCompositeAsInnerClass() {
		// for inner classes you to use OuterWidget-InnerWidget
		// see https://wiki.eclipse.org/Eclipse4/RCP/CSS
		Composite compositeToTest = createTestCompositeAsInnerClass(
				"CompositeTest-1 { background-color: #FF0000; color: #0000FF }");
		assertEquals(RED, compositeToTest.getBackground().getRGB());
		assertEquals(BLUE, compositeToTest.getForeground().getRGB());
	}
}
