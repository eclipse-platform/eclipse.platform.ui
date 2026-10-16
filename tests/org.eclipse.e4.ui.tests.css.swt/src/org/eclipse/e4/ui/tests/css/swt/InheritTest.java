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
 *     Stefan Winkler - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class InheritTest {

	@RegisterExtension
	CssSwtEngine css = new CssSwtEngine();

	private Color redColor;

	static final RGB GREEN = new RGB(0, 255, 0);
	static final RGB BLUE = new RGB(0, 0, 255);
	static final RGB RED = new RGB(255, 0, 0);

	@BeforeEach
	public void setUp() {
		redColor = new Color(RED);
	}

	/**
	 * Test status quo: if a more general rule sets the background, it applies
	 * to widgets matching more specific rules with specify no explicit
	 * backgound-color.
	 */
	@Test
	void testBackgroundNoInherit() {
		Label labelToTest = createTestLabel(
				"Label { background-color: #00FF00; }\n"
						+ "Composite Label { color: #0000FF; }", true);
		assertEquals(BLUE, labelToTest.getForeground().getRGB());
		assertEquals(GREEN, labelToTest.getBackground().getRGB());
	}

	/**
	 * Test new 'inherit' handling: if a more general rule sets the background,
	 * it does not apply to the more specific rule when it specifies
	 * 'background-color: inherit;' Instead, the background-color is set to the
	 * background-color of the parent widget.
	 */
	@Test
	void testBackgroundInherit() throws Exception {
		Label labelToTest = createTestLabel(
				"""
					Label { background-color: #00FF00; }
					Composite { background-color: #FF0000; }\s
					Composite Label { background-color: inherit; color: #0000FF; }""",
						false);
		assertEquals(BLUE, labelToTest.getForeground().getRGB());
		assertEquals(RED, labelToTest.getBackground().getRGB());
	}

	/**
	 * Test new 'inherit' handling: if a more general rule sets the background,
	 * it does not apply to the more specific rule when it specifies
	 * 'background-color: inherit;' Instead, the background-color is set to the
	 * background-color of the parent widget.
	 */
	@Test
	void testBackgroundInheritsAlsoExplicitlySetColors()
			throws Exception {
		Label labelToTest = createTestLabel(
				"Label { background-color: #00FF00; }\n"
						+ "Composite Label { background-color: inherit; color: #0000FF; }",
						true);
		assertEquals(BLUE, labelToTest.getForeground().getRGB());
		assertEquals(RED, labelToTest.getBackground().getRGB());
	}

	/**
	 * A reset widget that had its own color gets it back even when it equals the
	 * parent's, otherwise it shows the parent's background image.
	 */
	@Test
	void testBackgroundReappliedAfterResetWhenEqualToParent() throws IOException {
		Shell shell = new Shell(css.getDisplay(), SWT.SHELL_TRIM);
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBackgroundMode(SWT.INHERIT_FORCE);
		Label label = new Label(composite, SWT.NONE);
		CSSEngine engine = css.createEngine("Composite { background-color: #FF0000; } Label { background-color: #0000FF; }");
		engine.applyStyles(shell, true);

		// several resets, so the record survives more than one
		restyle(engine, shell, "Composite, Label { background-color: #FF0000; }");
		restyle(engine, shell, "Composite { background-color: #FF0000; } Label { background-color: #0000FF; }");
		restyle(engine, shell, "Composite, Label { background-color: #FF0000; }");

		Image image = new Image(css.getDisplay(), 4, 4);
		try {
			composite.setBackgroundImage(image);
			assertNull(label.getBackgroundImage());
		} finally {
			image.dispose();
		}
	}

	/**
	 * A widget that never owned a color keeps inheriting the parent's, so it still
	 * shows the parent's background image.
	 */
	@Test
	void testBackgroundEqualToParentIsInherited() {
		Shell shell = new Shell(css.getDisplay(), SWT.SHELL_TRIM);
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBackgroundMode(SWT.INHERIT_FORCE);
		Label label = new Label(composite, SWT.NONE);
		CSSEngine engine = css.createEngine("Composite, Label { background-color: #FF0000; }");
		engine.applyStyles(shell, true);

		Image image = new Image(css.getDisplay(), 4, 4);
		try {
			composite.setBackgroundImage(image);
			assertEquals(image, label.getBackgroundImage());
		} finally {
			image.dispose();
		}
	}

	/**
	 * Create a test label on a red canvas. Then apply given styles.
	 *
	 * @param styleSheet
	 *            styles to apply
	 * @return the test label
	 */
	private Label createTestLabel(String styleSheet,
			boolean setCompositeBackgroundExplicitly) {
		Display display = css.getDisplay();
		CSSEngine engine = css.createEngine(styleSheet);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new FillLayout());
		if (setCompositeBackgroundExplicitly) {
			composite.setBackground(redColor);
		}

		Label labelToTest = new Label(composite, SWT.NONE);

		labelToTest.setText("Some label text");

		// Apply styles
		engine.applyStyles(shell, true);
		return labelToTest;
	}

	private static void restyle(CSSEngine engine, Shell shell, String styleSheet) throws IOException {
		engine.reset();
		engine.parseStyleSheet(new StringReader(styleSheet));
		engine.applyStyles(shell, true);
	}
}
