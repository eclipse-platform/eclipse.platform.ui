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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.junit.Test;


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
			assertEquals("FormColors did not return the same instance for key: " + KEYS_NON_NULL[i], colors[i], fColors.getColor(KEYS_NON_NULL[i]));
		for (int i = 0; i < KEYS_NULL.length; i++)
			assertEquals("FormColors did not return the same instance for key: " + KEYS_NULL[i], nullColors[i], fColors.getColor(KEYS_NULL[i]));
		assertEquals("FormColors did not return the same instance for getInactiveBackground()", inactiveBg,
				fColors.getInactiveBackground());
		assertEquals("FormColors did not return the same instance for getBackground()", bg, fColors.getBackground());
		assertEquals("FormColors did not return the same instance for getForeground()", fg, fColors.getForeground());
		assertEquals("FormColors did not return the same instance for getBorderColor()", bc, fColors.getBorderColor());
		boolean testBorderDispose = !bc.equals(fColors.getColor(IFormColors.BORDER));
		// Create a Color which is not used inside eclipse to test if this color
		// is disposed reliable when the FormColors object is disposed.
		Color testColor = fColors.createColor("test", 1, 2, 3);
		fColors.dispose();
		assertTrue("FormColors did not dispose key: test", testColor.isDisposed());
		assertTrue("FormColors did not dispose getInactiveBackground()", inactiveBg.isDisposed());
		assertFalse("FormColors disposed getBackground()", bg.isDisposed());
		assertFalse("FormColors disposed getForeground()", fg.isDisposed());
		if (testBorderDispose)
			assertFalse("FormColors disposed getBorderColor() when it shouldn't have", bc.isDisposed());
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
			assertEquals("Different concurrent instances of FormColors did not return the same Color for key: "
					+ KEYS_NON_NULL[i], colors[i], colors2[i]);
		for (int i = 0; i < KEYS_NULL.length; i++)
			assertEquals("Different concurrent instances of FormColors did not return the same Color for key: "
					+ KEYS_NULL[i], nullColors[i], nullColors2[i]);
		assertEquals(
				"Different concurrent instances of FormColors did not return the same Color for getInactiveBackground()",
				inactiveBg, inactiveBg2);
		assertEquals("Different concurrent instances of FormColors did not return the same Color for getBackground()",
				bg, bg2);
		assertEquals("Different concurrent instances of FormColors did not return the same Color for getForeground()",
				fg, fg2);
		assertEquals("Different concurrent instances of FormColors did not return the same Color for getBorderColor()",
				bc, bc2);
		fColors2.dispose();
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			assertFalse("FormColors disposed different instance's key: " + KEYS_NON_NULL[i], colors[i].isDisposed());
		for (int i = 0; i < KEYS_NULL.length; i++)
			assertFalse("FormColors disposed different instance's key: " + KEYS_NULL[i],
					nullColors[i] != null && nullColors[i].isDisposed());
		assertFalse("FormColors disposed different instance's getInactiveBackground()", inactiveBg.isDisposed());
		assertFalse("FormColors disposed different instance's getBackground()", bg.isDisposed());
		assertFalse("FormColors disposed different instance's getForeground()", fg.isDisposed());
		assertFalse("FormColors disposed different instance's getBorderColor()", bc.isDisposed());
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
			assertFalse("FormToolkit disposed shared FormColor's key: " + KEYS_NON_NULL[i], colors[i].isDisposed());
		for (int i = 0; i < KEYS_NULL.length; i++)
			assertFalse("FormToolkit disposed shared FormColor's key: " + KEYS_NULL[i],
					nullColors[i] != null && nullColors[i].isDisposed());
		assertFalse("FormToolkit disposed shared FormColor's getInactiveBackground()", inactiveBg.isDisposed());
		assertFalse("FormToolkit disposed shared FormColor's getBackground()", bg.isDisposed());
		assertFalse("FormToolkit disposed shared FormColor's getForeground()", fg.isDisposed());
		assertFalse("FormToolkit disposed shared FormColor's getBorderColor()", bc.isDisposed());
		tk.dispose();
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			assertFalse("Last FormToolkit disposed shared FormColor's key: " + KEYS_NON_NULL[i],
					colors[i].isDisposed());
		for (int i = 0; i < KEYS_NULL.length; i++)
			assertFalse("Last FormToolkit disposed shared FormColor's key: " + KEYS_NULL[i],
					nullColors[i] != null && nullColors[i].isDisposed());
		assertFalse("Last FormToolkit disposed shared FormColor's getInactiveBackground()", inactiveBg.isDisposed());
		assertFalse("Last FormToolkit disposed shared FormColor's getBackground()", bg.isDisposed());
		assertFalse("Last FormToolkit disposed shared FormColor's getForeground()", fg.isDisposed());
		if (testBorderDispose)
			assertFalse("Last FormToolkit with shared FormColors disposed getBorderColor() when it shouldn't have",
					bc.isDisposed());
		fColors.dispose();
	}

	@Test
	public void testCustom() {
		FormColors fColors = new FormColors(Display.getCurrent());
		Color test1 = fColors.createColor(TEST_KEY_1, 255, 155, 55);
		Color test2 = fColors.createColor(TEST_KEY_2, 55, 155, 255);
		assertEquals("FormColors returned wrong color for an existing key.", fColors.getColor(TEST_KEY_1), test1);
		assertEquals("FormColors returned wrong color for an existing key.", fColors.getColor(TEST_KEY_2), test2);
		fColors.dispose();
		assertTrue("FormColors did not dispose a custom key.", test1.isDisposed());
		assertTrue("FormColors did not dispose a custom key.", test2.isDisposed());
	}
}
