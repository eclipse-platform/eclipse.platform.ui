/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.themes;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * Tests the pushing down of current theme changes into JFace.
 *
 * @since 3.0
 */
public class JFaceThemeTest extends ThemeTest {

    public JFaceThemeTest(String testName) {
        super(testName);
    }

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
        {
            ColorRegistry jfaceColors = JFaceResources.getColorRegistry();
            ColorRegistry themeColors = theme.getColorRegistry();
            assertTrue(jfaceColors.getKeySet().containsAll(
                    themeColors.getKeySet()));
            for (Object element : themeColors.getKeySet()) {
                String key = (String) element;
                assertEquals(themeColors.getRGB(key), jfaceColors.getRGB(key));
            }
        }
        JFaceResources.getFontRegistry().removeListener(listener);
        JFaceResources.getColorRegistry().removeListener(listener);
    }

    /**
     * TODO: detailed checking of the events
     */
    public void testPushdown() {
        ThemePropertyListener listener = new ThemePropertyListener();
        setAndTest(THEME1, listener);
        // ten changes, not the apparent 6 - remember the changes for the defaulted elements
        assertEquals(10, listener.getEvents().size());
        listener.getEvents().clear();
        setAndTest(IThemeManager.DEFAULT_THEME, listener);
        assertEquals(10, listener.getEvents().size());
    }

    /**
	 * Tests to ensure correct behavior of getColorDescriptor methods.
	 */
	public void testDefaultColorDescriptor() {
		ColorDescriptor desc = getDefaultTheme().getColorRegistry()
				.getColorDescriptor("somegarbage");
		assertNotNull(desc);
		Color color = desc.createColor(getWorkbench().getDisplay());
		assertNotNull(color);
		color.dispose();

		desc = getDefaultTheme().getColorRegistry().getColorDescriptor(
				"somegarbage", null);
		assertNull(desc);

		desc = getDefaultTheme().getColorRegistry().getColorDescriptor(
				"somegarbage", ColorDescriptor.createFrom(new RGB(0, 0, 0)));
		assertNotNull(desc);
		color = desc.createColor(getWorkbench().getDisplay());
		assertNotNull(color);
		color.dispose();
	}
}
