/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.themes;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.themes.ThemeElementHelper;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * Tests the theme API.
 * 
 * @since 3.0
 */
public class ThemeAPITest extends ThemeTest {

    /**
     * @param testName
     */
    public ThemeAPITest(String testName) {
        super(testName);
    }

    private void checkEvents(ThemePropertyListener listener, Object source,
            Object oldObject, Object newObject) {
        boolean array = oldObject instanceof Object[];
        List events = listener.getEvents();
        assertEquals(2, events.size());
        PropertyChangeEvent event = (PropertyChangeEvent) events.get(0);

        assertEquals(source, event.getSource());
        if (array) {
            assertArrayEquals((Object[]) oldObject, (Object[]) event
                    .getOldValue());
            assertArrayEquals((Object[]) newObject, (Object[]) event
                    .getNewValue());
        } else {
            assertEquals(oldObject, event.getOldValue());
            assertEquals(newObject, event.getNewValue());
        }

        event = (PropertyChangeEvent) events.get(1);
        assertEquals(source, event.getSource());
        if (array) {
            assertArrayEquals((Object[]) oldObject, (Object[]) event
                    .getNewValue());
            assertArrayEquals((Object[]) newObject, (Object[]) event
                    .getOldValue());
        } else {
            assertEquals(oldObject, event.getNewValue());
            assertEquals(newObject, event.getOldValue());
        }
    }

    public void testBooleanDataConversion() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(false, defaultTheme.getBoolean("data1"));
        assertEquals(false, defaultTheme.getBoolean("data2"));
        assertEquals(false, defaultTheme.getBoolean("int1"));
        assertEquals(false, defaultTheme.getBoolean("BOGUSKEY"));
        assertEquals(true, defaultTheme.getBoolean("bool1"));
    }

    public void testColorCascadeEvents() {
        ITheme currentTheme = fManager.getCurrentTheme();
        assertNotNull(currentTheme);

        ThemePropertyListener managerListener = new ThemePropertyListener();
        ThemePropertyListener themeListener = new ThemePropertyListener();
        fManager.addPropertyChangeListener(managerListener);
        currentTheme.addPropertyChangeListener(themeListener);

        ColorRegistry colorRegistry = currentTheme.getColorRegistry();
        RGB oldColor = colorRegistry.getRGB("rgbcolor");
        RGB newColor = new RGB(121, 9, 121);
        colorRegistry.put("rgbcolor", newColor);
        colorRegistry.put("rgbcolor", oldColor);

        checkEvents(managerListener, colorRegistry, oldColor, newColor);
        checkEvents(themeListener, colorRegistry, oldColor, newColor);

        fManager.removePropertyChangeListener(managerListener);
        currentTheme.removePropertyChangeListener(themeListener);
    }

    public void testColorFactory() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(TestColorFactory.RGB, defaultTheme.getColorRegistry()
                .getRGB("factorycolor"));
    }

    public void testColorPreferenceListener() {
        IPreferenceStore store = fWorkbench.getPreferenceStore();
        ITheme defaultTheme = getDefaultTheme();
        ITheme theme1 = getTheme1();

        testOverrideColorPreference(defaultTheme, store, "swtcolor");
        testOverrideColorPreference(theme1, store, "swtcolor");
        testOverrideColorPreference(defaultTheme, store, "rgbcolor");
        testOverrideColorPreference(theme1, store, "rgbcolor");
        testOverrideColorPreference(defaultTheme, store, "defaultedcolor");
        testOverrideColorPreference(theme1, store, "defaultedcolor");
        testNoOverrideColorPreference(defaultTheme, store, "nooverridecolor");
        testNoOverrideColorPreference(theme1, store, "nooverridecolor");
    }

    public void testDataKeySet() {
        ITheme defaultTheme = getDefaultTheme();
        Set themeKeys = defaultTheme.keySet();
        assertTrue(themeKeys.contains("data1"));
        assertTrue(themeKeys.contains("data2"));
        assertTrue(themeKeys.contains("int1"));
        assertTrue(themeKeys.contains("bool1"));
        assertFalse(themeKeys.contains("BOGUSKEY"));
    }

    public void testDataOverride() {
        ITheme theme1 = getTheme1();
        assertEquals("override1", theme1.getString("data1"));
        assertEquals("value2", theme1.getString("data2"));
    }

    public void testDefaultedColor() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(defaultTheme.getColorRegistry().getRGB("rgbcolor"),
                defaultTheme.getColorRegistry().getRGB("defaultedcolor"));
        assertEquals(defaultTheme.getColorRegistry().getRGB("defaultedcolor"),
                defaultTheme.getColorRegistry().getRGB("defaultedcolor2"));
        assertEquals(defaultTheme.getColorRegistry().getRGB("defaultedcolor2"),
                defaultTheme.getColorRegistry().getRGB("defaultedcolor3"));
    }

    public void testDefaultedFont() {
        ITheme defaultTheme = getDefaultTheme();
        assertArrayEquals(
                defaultTheme.getFontRegistry().getFontData("valfont"),
                defaultTheme.getFontRegistry().getFontData("defaultedfont"));
        assertArrayEquals(defaultTheme.getFontRegistry().getFontData(
                "defaultedfont"), defaultTheme.getFontRegistry().getFontData(
                "defaultedfont2"));
        assertArrayEquals(defaultTheme.getFontRegistry().getFontData(
                "defaultedfont2"), defaultTheme.getFontRegistry().getFontData(
                "defaultedfont3"));

    }

    public void testDefaultedFontOverride() {
        ITheme theme1 = getTheme1();
        assertArrayEquals(theme1.getFontRegistry().getFontData("valfont"),
                theme1.getFontRegistry().getFontData("defaultedfont"));
        assertArrayEquals(new FontData[] { new FontData("Courier", 16,
                SWT.NORMAL)}, theme1.getFontRegistry().getFontData(
                "defaultedfont2"));
        assertArrayEquals(theme1.getFontRegistry()
                .getFontData("defaultedfont2"), theme1.getFontRegistry()
                .getFontData("defaultedfont3"));
    }

    public void testDefaultedOverrideColor() {
        ITheme theme1 = getTheme1();
        assertEquals(theme1.getColorRegistry().getRGB("rgbcolor"), theme1
                .getColorRegistry().getRGB("defaultedcolor"));
        assertEquals(new RGB(9, 9, 9), theme1.getColorRegistry().getRGB(
                "defaultedcolor2"));
        assertEquals(theme1.getColorRegistry().getRGB("defaultedcolor2"),
                theme1.getColorRegistry().getRGB("defaultedcolor3"));
    }

    public void testFontCascadeEvents() {
        ITheme currentTheme = fManager.getCurrentTheme();
        assertNotNull(currentTheme);

        ThemePropertyListener managerListener = new ThemePropertyListener();
        ThemePropertyListener themeListener = new ThemePropertyListener();
        fManager.addPropertyChangeListener(managerListener);
        currentTheme.addPropertyChangeListener(themeListener);

        FontRegistry fontRegistry = currentTheme.getFontRegistry();
        FontData[] oldFont = fontRegistry.getFontData("valfont");
        FontData[] newFont = new FontData[] { new FontData("Courier", 30,
                SWT.ITALIC)};
        fontRegistry.put("valfont", newFont);
        fontRegistry.put("valfont", oldFont);

        checkEvents(managerListener, fontRegistry, oldFont, newFont);
        checkEvents(themeListener, fontRegistry, oldFont, newFont);

        fManager.removePropertyChangeListener(managerListener);
        currentTheme.removePropertyChangeListener(themeListener);
    }

    public void testFontPreferenceListener() {
        IPreferenceStore store = fWorkbench.getPreferenceStore();
        ITheme defaultTheme = getDefaultTheme();
        ITheme theme1 = getTheme1();

        testOverrideFontPreference(defaultTheme, store, "novalfont");
        testOverrideFontPreference(theme1, store, "novalfont");
        testOverrideFontPreference(defaultTheme, store, "valfont");
        testOverrideFontPreference(theme1, store, "valfont");
        testOverrideFontPreference(defaultTheme, store, "defaultedfont");
        testOverrideFontPreference(theme1, store, "defaultedfont");
        testNoOverrideFontPreference(defaultTheme, store, "nooverridefont");
        testNoOverrideFontPreference(theme1, store, "nooverridefont");
    }

    public void testGetBadTheme() {
        ITheme badTheme = fManager.getTheme(BOGUSID);
        assertNull(badTheme);
    }

    public void testIntDataConversion() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(0, defaultTheme.getInt("data1"));
        assertEquals(0, defaultTheme.getInt("data2"));
        assertEquals(0, defaultTheme.getInt("bool1"));
        assertEquals(0, defaultTheme.getInt("BOGUSKEY"));
        assertEquals(3133, defaultTheme.getInt("int1"));
    }

    private void testNoOverrideColorPreference(ITheme theme,
            IPreferenceStore store, String color) {
        RGB oldRGB = theme.getColorRegistry().getRGB(color);
        RGB newRGB = new RGB(75, 21, 68);

        store.setValue(ThemeElementHelper.createPreferenceKey(theme, color),
                StringConverter.asString(newRGB));
        assertEquals(getDefaultTheme().getColorRegistry().getRGB(color), theme
                .getColorRegistry().getRGB(color));
        store
                .setToDefault(ThemeElementHelper.createPreferenceKey(theme,
                        color));
        assertEquals(oldRGB, theme.getColorRegistry().getRGB(color));
    }

    private void testNoOverrideFontPreference(ITheme theme,
            IPreferenceStore store, String font) {
        FontData[] oldFont = theme.getFontRegistry().getFontData(font);
        FontData[] newFont = new FontData[] { new FontData("Courier", 30,
                SWT.ITALIC)};
        store.setValue(ThemeElementHelper.createPreferenceKey(theme, font),
                PreferenceConverter.getStoredRepresentation(newFont));
        assertArrayEquals(
                getDefaultTheme().getFontRegistry().getFontData(font), theme
                        .getFontRegistry().getFontData(font));
        store.setToDefault(ThemeElementHelper.createPreferenceKey(theme, font));
        assertArrayEquals(oldFont, theme.getFontRegistry().getFontData(font));
    }

    public void testNoValFont() {
        ITheme defaultTheme = getDefaultTheme();
        assertArrayEquals(defaultTheme.getFontRegistry().defaultFont()
                .getFontData(), defaultTheme.getFontRegistry().getFontData(
                "novalfont"));
    }

    public void testNoValFontOverride() {
        ITheme theme1 = getTheme1();
        assertArrayEquals(new FontData[] { new FontData("Courier", 10,
                SWT.ITALIC)}, theme1.getFontRegistry().getFontData("novalfont"));

    }

    private void testOverrideColorPreference(ITheme theme,
            IPreferenceStore store, String color) {
        RGB oldRGB = theme.getColorRegistry().getRGB(color);
        RGB newRGB = new RGB(75, 21, 68);

        store.setValue(ThemeElementHelper.createPreferenceKey(theme, color),
                StringConverter.asString(newRGB));
        assertEquals(newRGB, theme.getColorRegistry().getRGB(color));
        store
                .setToDefault(ThemeElementHelper.createPreferenceKey(theme,
                        color));
        assertEquals(oldRGB, theme.getColorRegistry().getRGB(color));
    }

    private void testOverrideFontPreference(ITheme theme,
            IPreferenceStore store, String font) {
        FontData[] oldFont = theme.getFontRegistry().getFontData(font);
        FontData[] newFont = new FontData[] { new FontData("Courier", 30,
                SWT.ITALIC)};
        store.setValue(ThemeElementHelper.createPreferenceKey(theme, font),
                PreferenceConverter.getStoredRepresentation(newFont));
        assertArrayEquals(newFont, theme.getFontRegistry().getFontData(font));
        store.setToDefault(ThemeElementHelper.createPreferenceKey(theme, font));
        assertArrayEquals(oldFont, theme.getFontRegistry().getFontData(font));
    }

    public void testPlatformColor() {
        ITheme defaultTheme = getDefaultTheme();
        RGB rgb = null;
        // test for two specific platforms and one general
        if (Platform.getWS().equals("win32")
                && Platform.getOS().equals("win32"))
            rgb = new RGB(50, 50, 50);
        else if (Platform.getWS().equals("gtk")
                && Platform.getOS().equals("linux"))
            rgb = new RGB(25, 25, 25);
        else if (Platform.getOS().equals("linux"))
            rgb = new RGB(75, 75, 75);
        else
            rgb = new RGB(0, 0, 0);

        assertEquals(rgb, defaultTheme.getColorRegistry().getRGB(
                "platformcolor"));
    }

    public void testPlatformFont() {
        ITheme defaultTheme = getDefaultTheme();
        FontData[] data = null;
        if (Platform.getWS().equals("win32")
                && Platform.getOS().equals("win32"))
            data = new FontData[] { new FontData("Courier New", 12, SWT.NORMAL)};
        else
            data = new FontData[] { new FontData("Sans", 15, SWT.BOLD)};

        assertArrayEquals(data, defaultTheme.getFontRegistry().getFontData(
                "platformfont"));
    }

    public void testRGBColor() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(new RGB(1, 1, 2), defaultTheme.getColorRegistry().getRGB(
                "rgbcolor"));
    }

    public void testRGBColorOverride() {
        ITheme theme1 = getTheme1();
        assertEquals(new RGB(2, 1, 1), theme1.getColorRegistry().getRGB(
                "rgbcolor"));
    }

    public void testSetTheme() {
        ThemePropertyListener listener = new ThemePropertyListener();
        fManager.addPropertyChangeListener(listener);
        ITheme currentTheme = fManager.getCurrentTheme();
        fManager.setCurrentTheme(BOGUSID);
        assertEquals(currentTheme, fManager.getCurrentTheme());
        fManager.setCurrentTheme(THEME1);
        assertNotSame(currentTheme, fManager.getCurrentTheme());
        ITheme newCurrentTheme = fManager.getCurrentTheme();
        ITheme theme1 = getTheme1();
        assertEquals(theme1, newCurrentTheme);
        List events = listener.getEvents();
        assertEquals(1, events.size());
        PropertyChangeEvent event = ((PropertyChangeEvent) events.get(0));
        assertEquals(IThemeManager.CHANGE_CURRENT_THEME, event.getProperty());
        assertEquals(currentTheme, event.getOldValue());
        assertEquals(newCurrentTheme, event.getNewValue());
        fManager.removePropertyChangeListener(listener);
    }

    public void testStringData() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals("value1", defaultTheme.getString("data1"));
        assertEquals("value2", defaultTheme.getString("data2"));
        assertEquals("3133", defaultTheme.getString("int1"));
        assertEquals("true", defaultTheme.getString("bool1"));
        assertEquals(null, defaultTheme.getString("BOGUSKEY"));
    }

    public void testSWTColor() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE)
                .getRGB(), defaultTheme.getColorRegistry().getRGB("swtcolor"));
    }

    public void testSWTColorOverride() {
        ITheme theme1 = getTheme1();
        assertEquals(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN)
                .getRGB(), theme1.getColorRegistry().getRGB("swtcolor"));
    }

    public void testThemeDescriptions() {
        {
            ITheme defaultTheme = getDefaultTheme();
            assertEquals(IThemeManager.DEFAULT_THEME, defaultTheme.getId());
            // don't bother testing against the actual value
            assertNotNull(defaultTheme.getLabel());
        }
        {
            ITheme theme1 = getTheme1();
            assertEquals(THEME1, theme1.getId());
            assertEquals("test theme 1", theme1.getLabel());
        }
    }

    public void testValFont() {
        ITheme defaultTheme = getDefaultTheme();
        assertArrayEquals(
                new FontData[] { new FontData("Tahoma", 20, SWT.BOLD)},
                defaultTheme.getFontRegistry().getFontData("valfont"));
    }
}