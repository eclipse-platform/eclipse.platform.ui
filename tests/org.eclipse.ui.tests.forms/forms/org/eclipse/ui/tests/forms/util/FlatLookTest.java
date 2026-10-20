/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     vogella GmbH - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.internal.forms.widgets.FormHeading;
import org.eclipse.ui.internal.forms.widgets.FormImages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FlatLookTest {
	private static Display display;

	static {
		try {
			display = PlatformUI.getWorkbench().getDisplay();
		} catch (Throwable e) {
			display = new Display();
		}
	}

	private Shell shell;

	@BeforeEach
	public void setUp() {
		shell = new Shell(display);
	}

	@AfterEach
	public void tearDown() {
		shell.dispose();
	}

	@Test
	public void testFormHeadingFlatLook() {
		Form form = new Form(shell, SWT.NONE);
		FormHeading head = (FormHeading) form.getHead();
		head.setSize(100, 50);

		Color color = new Color(255, 0, 0);
		Color[] identicalColors = new Color[] { color, color };
		int[] percents = new int[] { 100 };

		head.setTextBackground(identicalColors, percents, true);

		// Verify the heading can render without errors when gradient colors are
		// identical (flat look)
		assertDoesNotThrow(() -> {
			head.redraw();
			head.update();
		});

		// Verify with distinct colors as well to ensure both paths work
		Color color2 = new Color(0, 0, 255);
		Color[] distinctColors = new Color[] { color, color2 };
		head.setTextBackground(distinctColors, percents, true);

		assertDoesNotThrow(() -> {
			head.redraw();
			head.update();
		});
	}

	@Test
	public void testFlatLookUsesSolidBackground() {
		Form form = new Form(shell, SWT.NONE);
		FormHeading head = (FormHeading) form.getHead();
		head.setSize(100, 50);

		Color color = new Color(200, 100, 50);
		head.setTextBackground(new Color[] { color, color }, new int[] { 100 }, true);

		// Flat look: identical colors should use solid background, no gradient
		// image
		assertNull(head.getBackgroundImage(),
				"No gradient image should be created for identical colors");
		assertEquals(color.getRGB(), head.getBackground().getRGB(),
				"Background should be the flat color");
	}

	@Test
	public void testGradientLookUsesBackgroundImage() {
		Form form = new Form(shell, SWT.NONE);
		FormHeading head = (FormHeading) form.getHead();
		head.setSize(100, 50);

		Color color1 = new Color(255, 0, 0);
		Color color2 = new Color(0, 0, 255);
		head.setTextBackground(new Color[] { color1, color2 }, new int[] { 100 }, true);

		// Gradient look: distinct colors should generate a background image
		assertNotNull(head.getBackgroundImage(),
				"A gradient image should be created for distinct colors");
	}

	@Test
	public void testSwitchFromGradientToFlat() {
		Form form = new Form(shell, SWT.NONE);
		FormHeading head = (FormHeading) form.getHead();
		head.setSize(100, 50);

		// First set a real gradient
		Color color1 = new Color(255, 0, 0);
		Color color2 = new Color(0, 0, 255);
		head.setTextBackground(new Color[] { color1, color2 }, new int[] { 100 }, true);
		assertNotNull(head.getBackgroundImage());

		// Switch to flat look — gradient image should be cleaned up
		Color flat = new Color(128, 128, 128);
		head.setTextBackground(new Color[] { flat, flat }, new int[] { 100 }, true);
		assertNull(head.getBackgroundImage(),
				"Gradient image should be removed when switching to flat look");
		assertEquals(flat.getRGB(), head.getBackground().getRGB());
	}

	@Test
	public void testSingleColorArrayIsFlatLook() {
		Form form = new Form(shell, SWT.NONE);
		FormHeading head = (FormHeading) form.getHead();
		head.setSize(100, 50);

		Color color = new Color(42, 42, 42);
		head.setTextBackground(new Color[] { color }, new int[] { 100 }, true);

		assertNull(head.getBackgroundImage(),
				"Single-color array should be treated as flat look");
		assertEquals(color.getRGB(), head.getBackground().getRGB());
	}

	@Test
	public void testFlatLookRendersWithoutErrors() {
		Form form = new Form(shell, SWT.NONE);
		FormHeading head = (FormHeading) form.getHead();
		head.setSize(100, 50);

		Color color = new Color(200, 200, 200);
		head.setTextBackground(new Color[] { color, color }, new int[] { 100 }, true);

		assertDoesNotThrow(() -> {
			head.redraw();
			head.update();
		});
	}

	@Test
	public void testResetToNullClearsGradient() {
		Form form = new Form(shell, SWT.NONE);
		FormHeading head = (FormHeading) form.getHead();
		head.setSize(100, 50);

		Color color1 = new Color(255, 0, 0);
		Color color2 = new Color(0, 0, 255);
		head.setTextBackground(new Color[] { color1, color2 }, new int[] { 100 }, true);
		assertNotNull(head.getBackgroundImage());

		// Reset with null
		head.setTextBackground(null, null, true);
		assertNull(head.getBackgroundImage(),
				"Background image should be cleared on reset");
	}

	@Test
	public void testSectionFlatLook() {
		Section section = new Section(shell, Section.TITLE_BAR);
		Color bg = new Color(240, 240, 240);
		section.setTitleBarBackground(bg);
		section.setTitleBarBorderColor(bg);

		assertEquals(bg.getRGB(), section.getTitleBarBackground().getRGB());
		assertEquals(bg.getRGB(), section.getTitleBarBorderColor().getRGB());

		section.setSize(100, 100);
		section.redraw();
		section.update();
	}

	@Test
	public void testFormImagesFlatGradient() throws Exception {
		FormImages instance = FormImages.getInstance();
		Color color = new Color(100, 100, 100);

		// test simple gradient with identical colors
		org.eclipse.swt.graphics.Image img1 = instance.getGradient(color, color, 10, 10, 0, display);
		assertNotNull(img1);
		instance.markFinished(img1, display);

		// test complex gradient with identical colors
		org.eclipse.swt.graphics.Image img2 = instance.getGradient(new Color[] { color, color }, new int[] { 100 }, 10,
				true, null, display);
		assertNotNull(img2);
		instance.markFinished(img2, display);

		// test section gradient with identical colors
		org.eclipse.swt.graphics.Image img3 = instance.getSectionGradientImage(color, color, 10, 10, 0, display);
		assertNotNull(img3);
		instance.markFinished(img3, display);
	}
}
