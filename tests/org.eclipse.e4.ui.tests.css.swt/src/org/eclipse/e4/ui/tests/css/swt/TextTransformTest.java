/*******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests the <code>text-transform</code> property.
 * <p>
 * Valid attribute values include:
 * </p>
 * <ol>
 * <li><code>capitalize</code></li>
 * <li><code>uppercase</code></li>
 * <li><code>lowercase</code></li>
 * </ol>
 * <p>
 * <a href="http://www.w3.org/TR/CSS2/text.html#caps-prop">CSS
 * <code>text-transform</code> specification from W3C</a>
 * </p>
 */
public abstract class TextTransformTest extends CSSSWTTestCase {

	private CSSEngine engine;

	/**
	 * Retrieves the name of the widget that is being tested, must not be
	 * <code>null</code>.
	 * 
	 * @return the name of the widget for identification by the style sheet
	 */
	protected abstract String getWidgetName();

	/**
	 * Creates and returns the control that will be tested for verifying that
	 * the <code>text-transform</code> property works, must not be
	 * <code>null</code>.
	 * 
	 * @param parent
	 *            the parent composite to house the control
	 * @return the created control, must not be <code>null</code>
	 */
	protected abstract Control createControl(Composite parent);

	/**
	 * Retrieves the text set to the specified control.
	 * 
	 * @param control
	 *            the control to retrieve text from
	 * @return the text that's set on the underlying control
	 */
	protected abstract String getText(Control control);

	/**
	 * Sets the specified string to the control.
	 * 
	 * @param control
	 *            the control to set the string to
	 * @param string
	 *            the string to set to the control
	 */
	protected abstract void setText(Control control, String string);

	private Control createTestControl(String styleSheet) {
		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Control controlToTest = createControl(shell);
		setText(controlToTest, "Some label text");

		// Apply styles
		engine.applyStyles(shell, true);

		shell.pack();
		return controlToTest;
	}

	/**
	 * Tests the <code>capitalize</code> attribute value.
	 */
	public void testTextTransformCapitalize() {
		Control controlToTest = createTestControl(getWidgetName()
				+ " { text-transform: capitalize; }");
		assertEquals("Some Label Text", getText(controlToTest));
	}

	/**
	 * Tests the <code>uppercase</code> attribute value.
	 */
	public void testTextTransformUpperCase() {
		Control controlToTest = createTestControl(getWidgetName()
				+ " { text-transform: uppercase; }");
		assertEquals("SOME LABEL TEXT", getText(controlToTest));
	}

	/**
	 * Tests the <code>lowercase</code> attribute value.
	 */
	public void testTextTransformLowerCase() {
		Control controlToTest = createTestControl(getWidgetName()
				+ " { text-transform: lowercase; }");
		assertEquals("some label text", getText(controlToTest));
	}
}
