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

		Color color = new Color(display, 255, 0, 0);
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
		Color color2 = new Color(display, 0, 0, 255);
		Color[] distinctColors = new Color[] { color, color2 };
		head.setTextBackground(distinctColors, percents, true);

		assertDoesNotThrow(() -> {
			head.redraw();
			head.update();
		});
	}

	@Test
	public void testSectionFlatLook() {
		Section section = new Section(shell, Section.TITLE_BAR);
		Color bg = new Color(display, 240, 240, 240);
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
		Color color = new Color(display, 100, 100, 100);

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
