/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.forms.util;

import junit.framework.TestCase;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.junit.Assert;


public class FormColorsTests extends TestCase {

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
		Assert.assertEquals("FormColors did not return the same instance for getInactiveBackground()", inactiveBg, fColors.getInactiveBackground());
		Assert.assertEquals("FormColors did not return the same instance for getBackground()", bg, fColors.getBackground());
		Assert.assertEquals("FormColors did not return the same instance for getForeground()", fg, fColors.getForeground());
		Assert.assertEquals("FormColors did not return the same instance for getBorderColor()", bc, fColors.getBorderColor());
		boolean testBorderDispose = !bc.equals(fColors.getColor(IFormColors.BORDER));
		fColors.dispose();
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			Assert.assertTrue("FormColors did not dispose key: " + KEYS_NON_NULL[i], colors[i].isDisposed());
		for (int i = 0; i < KEYS_NULL.length; i++)
			Assert.assertTrue("FormColors did not dispose key: " + KEYS_NULL[i], nullColors[i] == null || nullColors[i].isDisposed());
		Assert.assertTrue("FormColors did not dispose getInactiveBackground()", inactiveBg.isDisposed());
		Assert.assertFalse("FormColors disposed getBackground()", bg.isDisposed());
		Assert.assertFalse("FormColors disposed getForeground()", fg.isDisposed());
		if (testBorderDispose)
			Assert.assertFalse("FormColors disposed getBorderColor() when it shouldn't have", bc.isDisposed());
	}

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
			Assert.assertEquals("Different concurrent instances of FormColors did not return the same Color for key: " + KEYS_NON_NULL[i], colors[i], colors2[i]);
		for (int i = 0; i < KEYS_NULL.length; i++)
			Assert.assertEquals("Different concurrent instances of FormColors did not return the same Color for key: " + KEYS_NULL[i], nullColors[i], nullColors2[i]);
		Assert.assertEquals("Different concurrent instances of FormColors did not return the same Color for getInactiveBackground()", inactiveBg, inactiveBg2);
		Assert.assertEquals("Different concurrent instances of FormColors did not return the same Color for getBackground()", bg, bg2);
		Assert.assertEquals("Different concurrent instances of FormColors did not return the same Color for getForeground()", fg, fg2);
		Assert.assertEquals("Different concurrent instances of FormColors did not return the same Color for getBorderColor()", bc, bc2);
		fColors2.dispose();
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			Assert.assertFalse("FormColors disposed different instance's key: " + KEYS_NON_NULL[i] , colors[i].isDisposed());
		for (int i = 0; i < KEYS_NULL.length; i++)
			Assert.assertFalse("FormColors disposed different instance's key: " + KEYS_NULL[i], nullColors[i] != null && nullColors[i].isDisposed());
		Assert.assertFalse("FormColors disposed different instance's getInactiveBackground()", inactiveBg.isDisposed());
		Assert.assertFalse("FormColors disposed different instance's getBackground()", bg.isDisposed());
		Assert.assertFalse("FormColors disposed different instance's getForeground()", fg.isDisposed());
		Assert.assertFalse("FormColors disposed different instance's getBorderColor()", bc.isDisposed());
		fColors.dispose();
	}

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
			Assert.assertFalse("FormToolkit disposed shared FormColor's key: " + KEYS_NON_NULL[i] , colors[i].isDisposed());
		for (int i = 0; i < KEYS_NULL.length; i++)
			Assert.assertFalse("FormToolkit disposed shared FormColor's key: " + KEYS_NULL[i], nullColors[i] != null && nullColors[i].isDisposed());
		Assert.assertFalse("FormToolkit disposed shared FormColor's getInactiveBackground()", inactiveBg.isDisposed());
		Assert.assertFalse("FormToolkit disposed shared FormColor's getBackground()", bg.isDisposed());
		Assert.assertFalse("FormToolkit disposed shared FormColor's getForeground()", fg.isDisposed());
		Assert.assertFalse("FormToolkit disposed shared FormColor's getBorderColor()", bc.isDisposed());
		tk.dispose();
		for (int i = 0; i < KEYS_NON_NULL.length; i++)
			Assert.assertFalse("Last FormToolkit disposed shared FormColor's key: " + KEYS_NON_NULL[i], colors[i].isDisposed());
		for (int i = 0; i < KEYS_NULL.length; i++)
			Assert.assertFalse("Last FormToolkit disposed shared FormColor's key: " + KEYS_NULL[i], nullColors[i] != null && nullColors[i].isDisposed());
		Assert.assertFalse("Last FormToolkit disposed shared FormColor's getInactiveBackground()", inactiveBg.isDisposed());
		Assert.assertFalse("Last FormToolkit disposed shared FormColor's getBackground()", bg.isDisposed());
		Assert.assertFalse("Last FormToolkit disposed shared FormColor's getForeground()", fg.isDisposed());
		if (testBorderDispose)
			Assert.assertFalse("Last FormToolkit with shared FormColors disposed getBorderColor() when it shouldn't have", bc.isDisposed());
		fColors.dispose();
	}

	public void testCustom() {
		FormColors fColors = new FormColors(Display.getCurrent());
		Color test1 = fColors.createColor(TEST_KEY_1, 255, 155, 55);
		Color test2 = fColors.createColor(TEST_KEY_2, 55, 155, 255);
		Assert.assertEquals("FormColors returned wrong color for an existing key.", fColors.getColor(TEST_KEY_1), test1);
		Assert.assertEquals("FormColors returned wrong color for an existing key.", fColors.getColor(TEST_KEY_2), test2);
		fColors.dispose();
		Assert.assertTrue("FormColors did not dispose a custom key.", test1.isDisposed());
		Assert.assertTrue("FormColors did not dispose a custom key.", test2.isDisposed());
	}
}
