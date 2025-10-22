/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
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
 *     Ralf M Petter<ralf.petter@gmail.com> - Bug 510241, 510830
 *******************************************************************************/

package org.eclipse.ui.tests.forms.util;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.junit.jupiter.api.Test;


public class FormColorsTest {

	// these keys should always return a Color until disposed
	private static String[] KEYS_NON_NULL = {
		IFormColors.BORDER,
		IFormColors.H_BOTTOM_KEYLINE1,
		IFormColors.H_BOTTOM_KEYLINE2,
		IFormColors.H_GRADIENT_END,
		IFormColors.H_GRADIENT_START,
		IFormColors.H_HOVER_FULL,
		IFormColors.H_HOVER_LIGHT,
		IFormColors.SEPARATOR,
		IFormColors.TB_BG,
		IFormColors.TB_BORDER,
		IFormColors.TB_TOGGLE,
		IFormColors.TB_TOGGLE_HOVER
	};
	// these keys may return null
	private static String[] KEYS_NULL = {
		IFormColors.TB_FG
	};

	private static String TEST_KEY_1 = "testKey1";
	private static String TEST_KEY_2 = "testKey2";

	@Test
	public void testStandalone() {
		FormColors fColors = new FormColors(Display.getCurrent());
		Color[] colors = new Color[KEYS_NON_NULL.length];
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			colors[i] = fColors.getColor(KEYS_NON_NULL[i]);
		Color[] nullColors = new Color[KEYS_NULL.length];
		for (int i = 0; i < KEYS_NULL.length; i++)
			nullColors[i] = fColors.getColor(KEYS_NULL[i]);
		Color inactiveBg = fColors.getInactiveBackground();
		Color bg = fColors.getBackground();
		Color fg = fColors.getForeground();
		Color bc = fColors.getBorderColor();
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			assertEquals(colors[i], fColors.getColor(KEYS_NON_NULL[i]), "FormColors did not return the same instance for key: " + KEYS_NON_NULL[i]);
		for (int i = 0; i < KEYS_NULL.length; i++)
			assertEquals(nullColors[i], fColors.getColor(KEYS_NULL[i]), "FormColors did not return the same instance for key: " + KEYS_NULL[i]);
		assertEquals(inactiveBg, fColors.getInactiveBackground(),
				"FormColors did not return the same instance for getInactiveBackground()");
		assertEquals(bg, fColors.getBackground(), "FormColors did not return the same instance for getBackground()");
		assertEquals(fg, fColors.getForeground(), "FormColors did not return the same instance for getForeground()");
		assertEquals(bc, fColors.getBorderColor(), "FormColors did not return the same instance for getBorderColor()");
		fColors.dispose();
	}

	@Test
	public void testMultiple() {
		FormColors fColors = new FormColors(Display.getCurrent());
		Color[] colors = new Color[KEYS_NON_NULL.length];
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			colors[i] = fColors.getColor(KEYS_NON_NULL[i]);
		Color[] nullColors = new Color[KEYS_NULL.length];
		for (int i = 0; i < KEYS_NULL.length; i++)
			nullColors[i] = fColors.getColor(KEYS_NULL[i]);
		Color inactiveBg = fColors.getInactiveBackground();
		Color bg = fColors.getBackground();
		Color fg = fColors.getForeground();
		Color bc = fColors.getBorderColor();
		FormColors fColors2 = new FormColors(Display.getCurrent());
		Color[] colors2 = new Color[KEYS_NON_NULL.length];
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			colors2[i] = fColors2.getColor(KEYS_NON_NULL[i]);
		Color[] nullColors2 = new Color[KEYS_NULL.length];
		for (int i = 0; i < KEYS_NULL.length; i++)
			nullColors2[i] = fColors2.getColor(KEYS_NULL[i]);
		Color inactiveBg2 = fColors2.getInactiveBackground();
		Color bg2 = fColors2.getBackground();
		Color fg2 = fColors2.getForeground();
		Color bc2 = fColors2.getBorderColor();
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			assertEquals(colors[i], colors2[i], "Different concurrent instances of FormColors did not return the same Color for key: "
					+ KEYS_NON_NULL[i]);
		for (int i = 0; i < KEYS_NULL.length; i++)
			assertEquals(nullColors[i], nullColors2[i], "Different concurrent instances of FormColors did not return the same Color for key: "
					+ KEYS_NULL[i]);
		assertEquals(inactiveBg, inactiveBg2,
				"Different concurrent instances of FormColors did not return the same Color for getInactiveBackground()");
		assertEquals(bg, bg2, "Different concurrent instances of FormColors did not return the same Color for getBackground()");
		assertEquals(fg, fg2, "Different concurrent instances of FormColors did not return the same Color for getForeground()");
		assertEquals(bc, bc2, "Different concurrent instances of FormColors did not return the same Color for getBorderColor()");
		fColors2.dispose();
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			assertFalse(colors[i].isDisposed(), "FormColors disposed different instance's key: " + KEYS_NON_NULL[i]);
		for (int i = 0; i < KEYS_NULL.length; i++)
			assertFalse(nullColors[i] != null && nullColors[i].isDisposed(),
					"FormColors disposed different instance's key: " + KEYS_NULL[i]);
		assertFalse(inactiveBg.isDisposed(), "FormColors disposed different instance's getInactiveBackground()");
		assertFalse(bg.isDisposed(), "FormColors disposed different instance's getBackground()");
		assertFalse(fg.isDisposed(), "FormColors disposed different instance's getForeground()");
		assertFalse(bc.isDisposed(), "FormColors disposed different instance's getBorderColor()");
		fColors.dispose();
	}

	@Test
	public void testShared() {
		FormColors fColors = new FormColors(Display.getCurrent());
		fColors.markShared();
		FormToolkit tk = new FormToolkit(fColors);
		FormToolkit tk2 = new FormToolkit(fColors);
		Color[] colors = new Color[KEYS_NON_NULL.length];
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			colors[i] = tk.getColors().getColor(KEYS_NON_NULL[i]);
		Color[] nullColors = new Color[KEYS_NULL.length];
		for (int i = 0; i < KEYS_NULL.length; i++)
			nullColors[i] = tk.getColors().getColor(KEYS_NULL[i]);
		Color inactiveBg = tk.getColors().getInactiveBackground();
		Color bg = tk.getColors().getBackground();
		Color fg = tk.getColors().getForeground();
		Color bc = tk.getColors().getBorderColor();
		Color[] colors2 = new Color[KEYS_NON_NULL.length];
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			colors2[i] = tk2.getColors().getColor(KEYS_NON_NULL[i]);
		Color[] nullColors2 = new Color[KEYS_NULL.length];
		for (int i = 0; i < KEYS_NULL.length; i++)
			nullColors2[i] = tk2.getColors().getColor(KEYS_NULL[i]);
		boolean testBorderDispose = !bc.equals(fColors.getColor(IFormColors.BORDER));
		tk2.dispose();
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			assertFalse(colors[i].isDisposed(), "FormToolkit disposed shared FormColor\'s key: " + KEYS_NON_NULL[i]);
		for (int i = 0; i < KEYS_NULL.length; i++)
			assertFalse(nullColors[i] != null && nullColors[i].isDisposed(),
					"FormToolkit disposed shared FormColor\'s key: " + KEYS_NULL[i]);
		assertFalse(inactiveBg.isDisposed(), "FormToolkit disposed shared FormColor\'s getInactiveBackground()");
		assertFalse(bg.isDisposed(), "FormToolkit disposed shared FormColor\'s getBackground()");
		assertFalse(fg.isDisposed(), "FormToolkit disposed shared FormColor\'s getForeground()");
		assertFalse(bc.isDisposed(), "FormToolkit disposed shared FormColor\'s getBorderColor()");
		tk.dispose();
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			assertFalse(colors[i].isDisposed(),
					"Last FormToolkit disposed shared FormColor\'s key: " + KEYS_NON_NULL[i]);
		for (int i = 0; i < KEYS_NULL.length; i++)
			assertFalse(nullColors[i] != null && nullColors[i].isDisposed(),
					"Last FormToolkit disposed shared FormColor\'s key: " + KEYS_NULL[i]);
		assertFalse(inactiveBg.isDisposed(), "Last FormToolkit disposed shared FormColor\'s getInactiveBackground()");
		assertFalse(bg.isDisposed(), "Last FormToolkit disposed shared FormColor\'s getBackground()");
		assertFalse(fg.isDisposed(), "Last FormToolkit disposed shared FormColor\'s getForeground()");
		if (testBorderDispose)
			assertFalse(bc.isDisposed(),
					"Last FormToolkit with shared FormColors disposed getBorderColor() when it shouldn\'t have");
		fColors.dispose();
	}

	@Test
	public void testCustom() {
		FormColors fColors = new FormColors(Display.getCurrent());
		Color test1 = fColors.createColor(TEST_KEY_1, 255, 155, 55);
		Color test2 = fColors.createColor(TEST_KEY_2, 55, 155, 255);
		assertEquals(test1, fColors.getColor(TEST_KEY_1), "FormColors returned wrong color for an existing key.");
		assertEquals(test2, fColors.getColor(TEST_KEY_2), "FormColors returned wrong color for an existing key.");
		fColors.dispose();
	}
}
