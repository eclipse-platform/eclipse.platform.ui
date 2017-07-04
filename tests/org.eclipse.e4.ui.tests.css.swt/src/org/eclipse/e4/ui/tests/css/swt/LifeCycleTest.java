/*******************************************************************************
 * Copyright (c) 2017 vogella GmbH and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.Assert.assertEquals;

import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

/**
 * Test to ensure that the PartRenderingEngine can be used with an registered
 * CSS engie. This is useful for example in an e4 lifecycle hook, in which the
 * CSS engine is not yet initialized
 *
 */
public class LifeCycleTest extends CSSSWTTestCase {

	static final RGB RED = new RGB(255, 0, 0);
	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);
	private Label label;

	protected Shell createTestShell() {
		IEclipseContext ctx = EclipseContextFactory.create();

		String cssURI = "platform:/plugin/org.eclipse.e4.ui.tests.css.swt/css/cssfortest.css";
		ctx.set("applicationCSS", cssURI);
		Shell shell = new Shell(display, SWT.SHELL_TRIM);

		// Create widgets
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);
		Composite panel = new Composite(shell, SWT.NONE);
		panel.setLayout(new FillLayout());
		label = new Label(panel, SWT.NONE);
		label.setText("");


		shell.pack();
		PartRenderingEngine.initializeStyling(display, ctx);
		return shell;
	}


	@Test
	public void testLabelColorViaPartRenderingEngine() {
		createTestShell();
		assertEquals(RED, label.getBackground().getRGB());
		assertEquals(BLUE, label.getForeground().getRGB());
	}

}