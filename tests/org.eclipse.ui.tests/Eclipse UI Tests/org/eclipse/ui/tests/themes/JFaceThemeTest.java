/*******************************************************************************
 * Copyright (c) 2004, 2026 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.tests.themes;

import static org.eclipse.ui.PlatformUI.getWorkbench;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.internal.css.swt.ColorAndFontUtil;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.junit.Test;

/**
 * Tests the pushing down of current theme changes into JFace.
 *
 * @since 3.0
 */
public class JFaceThemeTest extends ThemeTest {

	private void setAndTest(String themeId, IPropertyChangeListener listener) {
		JFaceResources.getFontRegistry().addListener(listener);
		JFaceResources.getColorRegistry().addListener(listener);
		fManager.setCurrentTheme(themeId);
		ITheme theme = fManager.getTheme(themeId);
		assertEquals(theme, fManager.getCurrentTheme());
		{
			FontRegistry jfaceFonts = JFaceResources.getFontRegistry();
			FontRegistry themeFonts = theme.getFontRegistry();
			// don't test for equality - other tests (or clients) may be pushing
			// new items into jface
			assertTrue(jfaceFonts.getKeySet().containsAll(
					themeFonts.getKeySet()));
			for (Object element : themeFonts.getKeySet()) {
				String key = (String) element;
				assertArrayEquals(themeFonts.getFontData(key), jfaceFonts
						.getFontData(key));
			}
		}
		ColorRegistry jfaceColors = JFaceResources.getColorRegistry();
		ColorRegistry themeColors = theme.getColorRegistry();
		assertTrue(jfaceColors.getKeySet().containsAll(
				themeColors.getKeySet()));
		for (Object element : themeColors.getKeySet()) {
			String key = (String) element;
			assertEquals(themeColors.getRGB(key), jfaceColors.getRGB(key));
		}
		JFaceResources.getFontRegistry().removeListener(listener);
		JFaceResources.getColorRegistry().removeListener(listener);
	}

	/**
	 * TODO: detailed checking of the events
	 */
	@Test
	public void testPushdown() {
		ThemePropertyListener listener = new ThemePropertyListener();
		setAndTest(THEME1, listener);
		// ten changes, not the apparent 6 - remember the changes for the defaulted elements
		assertEquals(10, listener.getEvents().size());
		listener.getEvents().clear();
		setAndTest(IThemeManager.DEFAULT_THEME, listener);
		assertEquals(10, listener.getEvents().size());
	}

	@Test
	public void testColorAndFontProviderResolvesCurrentTheme() {
		IColorAndFontProvider provider = ColorAndFontUtil.getColorAndFontProvider();
		assertEquals(new RGB(1, 1, 2), provider.getColor("rgbcolor"));

		fManager.setCurrentTheme(THEME1);
		assertEquals(new RGB(2, 1, 1), provider.getColor("rgbcolor"));

		ITheme theme = fManager.getCurrentTheme();
		ColorRegistry themeColors = theme.getColorRegistry();
		for (String key : themeColors.getKeySet()) {
			assertEquals(themeColors.getRGB(key), provider.getColor(key));
		}
		FontRegistry themeFonts = theme.getFontRegistry();
		for (String key : themeFonts.getKeySet()) {
			assertArrayEquals(themeFonts.getFontData(key), provider.getFont(key));
		}
	}

	@Test
	public void testColorAndFontProviderIsUpdatedBeforeThemeListeners() {
		IColorAndFontProvider provider = ColorAndFontUtil.getColorAndFontProvider();
		List<RGB> seen = new ArrayList<>();
		IPropertyChangeListener listener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				seen.add(provider.getColor("rgbcolor"));
			}
		};
		fManager.addPropertyChangeListener(listener);
		try {
			fManager.setCurrentTheme(THEME1);
		} finally {
			fManager.removePropertyChangeListener(listener);
		}

		assertFalse(seen.isEmpty());
		for (RGB rgb : seen) {
			assertEquals(new RGB(2, 1, 1), rgb);
		}
	}

	@Test
	public void testColorAndFontProviderIsUpdatedBeforeThemeValueListeners() {
		IColorAndFontProvider provider = ColorAndFontUtil.getColorAndFontProvider();
		ColorRegistry themeColors = fManager.getCurrentTheme().getColorRegistry();
		RGB original = themeColors.getRGB("rgbcolor");
		List<RGB> seen = new ArrayList<>();
		IPropertyChangeListener listener = new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				seen.add(provider.getColor("rgbcolor"));
			}
		};
		fManager.addPropertyChangeListener(listener);
		try {
			themeColors.put("rgbcolor", new RGB(9, 9, 9));
		} finally {
			fManager.removePropertyChangeListener(listener);
			themeColors.put("rgbcolor", original);
		}

		assertFalse(seen.isEmpty());
		for (RGB rgb : seen) {
			assertEquals(new RGB(9, 9, 9), rgb);
		}
	}

	/**
	 * Tests to ensure correct behavior of getColorDescriptor methods.
	 */
	@Test
	public void testDefaultColorDescriptor() {
		ColorDescriptor desc = getDefaultTheme().getColorRegistry()
				.getColorDescriptor("somegarbage");
		assertNotNull(desc);
		Color color = desc.createColor(getWorkbench().getDisplay());
		assertNotNull(color);

		desc = getDefaultTheme().getColorRegistry().getColorDescriptor(
				"somegarbage", null);
		assertNull(desc);

		desc = getDefaultTheme().getColorRegistry().getColorDescriptor(
				"somegarbage", ColorDescriptor.createFrom(new RGB(0, 0, 0)));
		assertNotNull(desc);
		color = desc.createColor(getWorkbench().getDisplay());
		assertNotNull(color);
	}
}
