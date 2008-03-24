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
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * Tests the theme API.
 * 
 * @since 3.0
 */
public class ThemeAPITest extends ThemeTest {

    /**
	 * 
	 */
	private static final String EXTENDED_THEME3 = "extendedTheme3";
	/**
	 * 
	 */
	private static final String EXTENDED_THEME2 = "extendedTheme2";
	/**
	 * 
	 */
	private static final String EXTENDED_THEME1 = "extendedTheme1";
	/**
	 * 
	 */
	private static final String PLATFORMFONT = "platformfont";
	/**
	 * 
	 */
	private static final String PLATFORMCOLOR = "platformcolor";
	/**
	 * 
	 */
	private static final String NOOVERRIDEFONT = "nooverridefont";
	/**
	 * 
	 */
	private static final String NOVALFONT = "novalfont";
	/**
	 * 
	 */
	private static final String DEFAULTEDFONT3 = "defaultedfont3";
	/**
	 * 
	 */
	private static final String DEFAULTEDFONT2 = "defaultedfont2";
	/**
	 * 
	 */
	private static final String DEFAULTEDFONT = "defaultedfont";
	/**
	 * 
	 */
	private static final String VALFONT = "valfont";
	/**
	 * 
	 */
	private static final String DEFAULTEDCOLOR3 = "defaultedcolor3";
	/**
	 * 
	 */
	private static final String DEFAULTEDCOLOR2 = "adefaultedcolor2";
	/**
	 * 
	 */
	private static final String VALUE2 = "value2";
	/**
	 * 
	 */
	private static final String OVERRIDE1 = "override1";
	/**
	 * 
	 */
	private static final String NOOVERRIDECOLOR = "nooverridecolor";
	/**
	 * 
	 */
	private static final String DEFAULTEDCOLOR = "defaultedcolor";
	/**
	 * 
	 */
	private static final String SWTCOLOR = "swtcolor";
	/**
	 * 
	 */
	private static final String FACTORYCOLOR = "factorycolor";
	/**
	 * 
	 */
	private static final String RGBCOLOR = "rgbcolor";
	/**
	 * 
	 */
	private static final String BOOL1 = "bool1";
	/**
	 * 
	 */
	private static final String BOGUSKEY = "BOGUSKEY";
	/**
	 * 
	 */
	private static final String INT1 = "int1";
	/**
	 * 
	 */
	private static final String DATA2 = "data2";
	/**
	 * 
	 */
	private static final String DATA1 = "data1";	
	/**
	 * 
	 */
	private static final String BAD_COLOR1 = "badColor1";
	/**
	 * 
	 */
	private static final String BAD_COLOR2 = "badColor2";
	/**
	 * 
	 */
	private static final String BAD_COLOR3 = "badColor3";

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
        assertEquals(false, defaultTheme.getBoolean(DATA1));
        assertEquals(false, defaultTheme.getBoolean(DATA2));
        assertEquals(false, defaultTheme.getBoolean(INT1));
        assertEquals(false, defaultTheme.getBoolean(BOGUSKEY));
        assertEquals(true, defaultTheme.getBoolean(BOOL1));
    }

    public void testColorCascadeEvents() {
        ITheme currentTheme = fManager.getCurrentTheme();
        assertNotNull(currentTheme);

        ThemePropertyListener managerListener = new ThemePropertyListener();
        ThemePropertyListener themeListener = new ThemePropertyListener();
        fManager.addPropertyChangeListener(managerListener);
        currentTheme.addPropertyChangeListener(themeListener);

        ColorRegistry colorRegistry = currentTheme.getColorRegistry();
        RGB oldColor = colorRegistry.getRGB(RGBCOLOR);
        RGB newColor = new RGB(121, 9, 121);
        colorRegistry.put(RGBCOLOR, newColor);
        colorRegistry.put(RGBCOLOR, oldColor);

        checkEvents(managerListener, colorRegistry, oldColor, newColor);
        checkEvents(themeListener, colorRegistry, oldColor, newColor);

        fManager.removePropertyChangeListener(managerListener);
        currentTheme.removePropertyChangeListener(themeListener);
    }

    public void testColorFactory() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(TestColorFactory.RGB, defaultTheme.getColorRegistry()
                .getRGB(FACTORYCOLOR));
    }

    public void testColorPreferenceListener_def_swtcolor() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme defaultTheme = getDefaultTheme();

        testOverrideColorPreference(defaultTheme, store, SWTCOLOR);
    }

    public void testColorPreferenceListener_def_rgbcolor() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme defaultTheme = getDefaultTheme();

        testOverrideColorPreference(defaultTheme, store, RGBCOLOR);
    }

    public void testColorPreferenceListener_def_defaultedcolor() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme defaultTheme = getDefaultTheme();

        testOverrideColorPreference(defaultTheme, store, DEFAULTEDCOLOR);
    }

    public void testColorPreferenceListener_def_nooverridecolor() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme defaultTheme = getDefaultTheme();

        testOverrideColorPreference(defaultTheme, store, NOOVERRIDECOLOR);
    }

    public void testColorPreferenceListener_th1_swtcolor() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme theme1 = getTheme1();

        testOverrideColorPreference(theme1, store, SWTCOLOR);
    }

    public void testColorPreferenceListener_th1_rgbcolor() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme theme1 = getTheme1();

        testOverrideColorPreference(theme1, store, RGBCOLOR);
    }

    public void testColorPreferenceListener_th1_defaultedcolor() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme theme1 = getTheme1();

        testOverrideColorPreference(theme1, store, DEFAULTEDCOLOR);
    }

    public void testColorPreferenceListener_th1_nooverridecolor() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme theme1 = getTheme1();

        testOverrideColorPreference(theme1, store, NOOVERRIDECOLOR);
    }

    public void testDataKeySet_data1() {
        ITheme defaultTheme = getDefaultTheme();
        Set themeKeys = defaultTheme.keySet();

        assertTrue(themeKeys.contains(DATA1));
    }

    public void testDataKeySet_data2() {
        ITheme defaultTheme = getDefaultTheme();
        Set themeKeys = defaultTheme.keySet();

        assertTrue(themeKeys.contains(DATA2));
    }

    public void testDataKeySet_int1() {
        ITheme defaultTheme = getDefaultTheme();
        Set themeKeys = defaultTheme.keySet();

        assertTrue(themeKeys.contains(INT1));
    }

    public void testDataKeySet_bool1() {
        ITheme defaultTheme = getDefaultTheme();
        Set themeKeys = defaultTheme.keySet();

        assertTrue(themeKeys.contains(BOOL1));
    }

    public void testDataKeySet_BOGUSKEY() {
        ITheme defaultTheme = getDefaultTheme();
        Set themeKeys = defaultTheme.keySet();

        assertFalse(themeKeys.contains(BOGUSKEY));
    }

    public void testDataOverride_data1() {
        ITheme theme1 = getTheme1();

        assertEquals(OVERRIDE1, theme1.getString(DATA1));
    }

    public void testDataOverride_data2() {
        ITheme theme1 = getTheme1();

        assertEquals(VALUE2, theme1.getString(DATA2));
    }

    public void testDefaultedColor_rgbcolor() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(defaultTheme.getColorRegistry().getRGB(RGBCOLOR),
                defaultTheme.getColorRegistry().getRGB(DEFAULTEDCOLOR));
    }

    public void testDefaultedColor_defaultedcolor() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(defaultTheme.getColorRegistry().getRGB(DEFAULTEDCOLOR),
                defaultTheme.getColorRegistry().getRGB(DEFAULTEDCOLOR2));
    }

    public void testDefaultedColor_defaultedcolor2() {
        ITheme defaultTheme = getDefaultTheme();

        assertEquals(defaultTheme.getColorRegistry().getRGB(DEFAULTEDCOLOR2),
                defaultTheme.getColorRegistry().getRGB(DEFAULTEDCOLOR3));
    }

    public void testDefaultedFont_valfont() {
        ITheme defaultTheme = getDefaultTheme();
        assertArrayEquals(
                defaultTheme.getFontRegistry().getFontData(VALFONT),
                defaultTheme.getFontRegistry().getFontData(DEFAULTEDFONT));
    }

    public void testDefaultedFont_defaultedfont() {
        ITheme defaultTheme = getDefaultTheme();
        assertArrayEquals(defaultTheme.getFontRegistry().getFontData(
                DEFAULTEDFONT), defaultTheme.getFontRegistry().getFontData(
                DEFAULTEDFONT2));
    }

    public void testDefaultedFont_defaultedfont2() {
        ITheme defaultTheme = getDefaultTheme();
        assertArrayEquals(defaultTheme.getFontRegistry().getFontData(
                DEFAULTEDFONT2), defaultTheme.getFontRegistry().getFontData(
                DEFAULTEDFONT3));
    }

    public void testDefaultedFontOverride_valfont() {
        ITheme theme1 = getTheme1();
        assertArrayEquals(theme1.getFontRegistry().getFontData(VALFONT),
                theme1.getFontRegistry().getFontData(DEFAULTEDFONT));
    }

    public void testDefaultedFontOverride_defaultedfont2() {
        ITheme theme1 = getTheme1();

        assertArrayEquals(new FontData[] { new FontData("Courier", 16,
                SWT.NORMAL) }, theme1.getFontRegistry().getFontData(
                DEFAULTEDFONT2));
    }

    public void testDefaultedFontOverride_defaultedfont3() {
        ITheme theme1 = getTheme1();

        assertArrayEquals(theme1.getFontRegistry()
                .getFontData(DEFAULTEDFONT2), theme1.getFontRegistry()
                .getFontData(DEFAULTEDFONT3));
    }

    public void testDefaultedOverrideColor_rgbcolor() {
        ITheme theme1 = getTheme1();
        assertEquals(theme1.getColorRegistry().getRGB(RGBCOLOR), theme1
                .getColorRegistry().getRGB(DEFAULTEDCOLOR));
    }

    public void testDefaultedOverrideColor_defaultedcolor2() {
        ITheme theme1 = getTheme1();
        assertEquals(new RGB(9, 9, 9), theme1.getColorRegistry().getRGB(
                DEFAULTEDCOLOR2));

    }

    public void testDefaultedOverrideColor_defaultedcolor3() {
        ITheme theme1 = getTheme1();
        assertEquals(theme1.getColorRegistry().getRGB(DEFAULTEDCOLOR2),
                theme1.getColorRegistry().getRGB(DEFAULTEDCOLOR3));
    }

    public void testFontCascadeEvents() {
        ITheme currentTheme = fManager.getCurrentTheme();
        assertNotNull(currentTheme);

        ThemePropertyListener managerListener = new ThemePropertyListener();
        ThemePropertyListener themeListener = new ThemePropertyListener();
        fManager.addPropertyChangeListener(managerListener);
        currentTheme.addPropertyChangeListener(themeListener);

        FontRegistry fontRegistry = currentTheme.getFontRegistry();
        FontData[] oldFont = fontRegistry.getFontData(VALFONT);
        FontData[] newFont = new FontData[] { new FontData("Courier", 30,
                SWT.ITALIC) };
        fontRegistry.put(VALFONT, newFont);
        fontRegistry.put(VALFONT, oldFont);

        checkEvents(managerListener, fontRegistry, oldFont, newFont);
        checkEvents(themeListener, fontRegistry, oldFont, newFont);

        fManager.removePropertyChangeListener(managerListener);
        currentTheme.removePropertyChangeListener(themeListener);
    }

    public void testFontPreferenceListener_def_novalfont() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme defaultTheme = getDefaultTheme();
        testOverrideFontPreference(defaultTheme, store, NOVALFONT);
    }

    public void testFontPreferenceListener_def_valfont() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme defaultTheme = getDefaultTheme();

        testOverrideFontPreference(defaultTheme, store, VALFONT);
    }

    public void testFontPreferenceListener_def_defaultedfont() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme defaultTheme = getDefaultTheme();

        testOverrideFontPreference(defaultTheme, store, DEFAULTEDFONT);
    }

    public void testFontPreferenceListener_def_nooverridefont() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme defaultTheme = getDefaultTheme();

        testOverrideFontPreference(defaultTheme, store, NOOVERRIDEFONT);
    }

    public void testFontPreferenceListener_th1_valfont() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme theme1 = getTheme1();

        testOverrideFontPreference(theme1, store, VALFONT);
    }

    public void testFontPreferenceListener_th1_novalfont() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme theme1 = getTheme1();

        testOverrideFontPreference(theme1, store, NOVALFONT);
    }

    public void testFontPreferenceListener_th1_defaultedfont() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme theme1 = getTheme1();

        testOverrideFontPreference(theme1, store, DEFAULTEDFONT);
    }

    public void testFontPreferenceListener_th1_nooverridefont() {
        IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
        ITheme theme1 = getTheme1();

        testOverrideFontPreference(theme1, store, NOOVERRIDEFONT);
    }

    public void testGetBadTheme() {
        ITheme badTheme = fManager.getTheme(BOGUSID);
        assertNull(badTheme);
    }

    public void testIntDataConversion() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(0, defaultTheme.getInt(DATA1));
        assertEquals(0, defaultTheme.getInt(DATA2));
        assertEquals(0, defaultTheme.getInt(BOOL1));
        assertEquals(0, defaultTheme.getInt(BOGUSKEY));
        assertEquals(3133, defaultTheme.getInt(INT1));
    }

    public void testNoValFont() {
        ITheme defaultTheme = getDefaultTheme();
        assertArrayEquals(defaultTheme.getFontRegistry().defaultFont()
                .getFontData(), defaultTheme.getFontRegistry().getFontData(
                NOVALFONT));
    }

    public void testNoValFontOverride() {
        ITheme theme1 = getTheme1();
        assertArrayEquals(new FontData[] { new FontData("Courier", 10,
                SWT.ITALIC) }, theme1.getFontRegistry()
                .getFontData(NOVALFONT));

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
                SWT.ITALIC) };
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
                PLATFORMCOLOR));
    }

    public void testPlatformFont() {
        ITheme defaultTheme = getDefaultTheme();
        FontData[] data = null;
        if (Platform.getWS().equals("win32")
                && Platform.getOS().equals("win32"))
            data = new FontData[] { new FontData("Courier New", 12, SWT.NORMAL) };
        else
            data = new FontData[] { new FontData("Sans", 15, SWT.BOLD) };

        assertArrayEquals(data, defaultTheme.getFontRegistry().getFontData(
                PLATFORMFONT));
    }

    public void testRGBColor() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(new RGB(1, 1, 2), defaultTheme.getColorRegistry().getRGB(
                RGBCOLOR));
    }

    public void testRGBColorOverride() {
        ITheme theme1 = getTheme1();
        assertEquals(new RGB(2, 1, 1), theme1.getColorRegistry().getRGB(
                RGBCOLOR));
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
        assertEquals("value1", defaultTheme.getString(DATA1));
        assertEquals(VALUE2, defaultTheme.getString(DATA2));
        assertEquals("3133", defaultTheme.getString(INT1));
        assertEquals("true", defaultTheme.getString(BOOL1));
        assertEquals(null, defaultTheme.getString(BOGUSKEY));
    }

    public void testSWTColor() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE)
                .getRGB(), defaultTheme.getColorRegistry().getRGB(SWTCOLOR));
    }

    public void testSWTColorOverride() {
        ITheme theme1 = getTheme1();
        assertEquals(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN)
                .getRGB(), theme1.getColorRegistry().getRGB(SWTCOLOR));
    }

    public void testThemeDescription_default() {
        ITheme defaultTheme = getDefaultTheme();
        assertEquals(IThemeManager.DEFAULT_THEME, defaultTheme.getId());
        // don't bother testing against the actual value
        assertNotNull(defaultTheme.getLabel());
    }

    public void testThemeDescription_theme1() {
        ITheme theme1 = getTheme1();
        assertEquals(THEME1, theme1.getId());
        assertEquals("test theme 1", theme1.getLabel());
    }

    public void testValFont() {
        ITheme defaultTheme = getDefaultTheme();
        assertArrayEquals(
                new FontData[] { new FontData("Tahoma", 20, SWT.BOLD) },
                defaultTheme.getFontRegistry().getFontData(VALFONT));
    }
    
    /*
     * The following tests check to ensure that support for multiple extensions
     * contributing to the same theeme work. They also check to ensure that the
     * first value encountered for a given font/colour is the only one used.
     */
    
    public void testThemeExtensionName() {
        ITheme ext1 = fManager.getTheme(EXTENDED_THEME1);
        ITheme ext2 = fManager.getTheme(EXTENDED_THEME2);
        ITheme ext3 = fManager.getTheme(EXTENDED_THEME3);
        
        assertEquals("Extended Theme 1", ext1.getLabel());
        assertEquals("Extended Theme 2", ext2.getLabel());
        assertEquals("Extended Theme 3", ext3.getLabel());
    }
    
    public void testThemeExtensionData() {
        ITheme ext1 = fManager.getTheme(EXTENDED_THEME1);
        assertNotNull(ext1.getString("d1"));
        assertEquals("d1", ext1.getString("d1"));
        assertNotNull(ext1.getString("d2"));
    }
    
    public void testThemeExtensionColor() {
        ITheme ext1 = fManager.getTheme(EXTENDED_THEME1);
        assertEquals(Display.getDefault().getSystemColor(SWT.COLOR_RED)
                .getRGB(), ext1.getColorRegistry().getRGB(SWTCOLOR)); 

        assertEquals(Display.getDefault().getSystemColor(SWT.COLOR_RED)
                .getRGB(), ext1.getColorRegistry().getRGB(RGBCOLOR)); 
    }
    
    public void testThemeExtensionFont() {
        ITheme ext1 = fManager.getTheme(EXTENDED_THEME1);
        
        FontData[] fd = new FontData[] { new FontData("Sans", 10,
                SWT.NORMAL) };
        
        assertArrayEquals(fd, ext1.getFontRegistry()
                .getFontData(VALFONT));

        assertArrayEquals(fd, ext1.getFontRegistry()
                .getFontData(NOVALFONT));
    }
    
    /**
	 * Tests to ensure that a color with a bogus value doesn't take down the
	 * workbench.
	 */
	public void testBadColor1() {
    	 ITheme defaultTheme = getDefaultTheme();
    	 assertEquals(new RGB(0,0,0), defaultTheme.getColorRegistry().getRGB(BAD_COLOR1)); // doesn't look like an RGB
	}
	
	/**
	 * Tests to ensure that a color with extra spaces doesn't take down the workbench.
	 */
	public void testBadColor2() {
    	 assertEquals(new RGB(0,0,1), getDefaultTheme().getColorRegistry().getRGB(BAD_COLOR2)); // rgb with extra spaces
	}
	
	/**
	 * Tests to ensure that a color with extra spaces doesn't take down the workbench.
	 */
	public void testBadColor3() {
    	 assertEquals(new RGB(0,0,0), getDefaultTheme().getColorRegistry().getRGB(BAD_COLOR3)); // rgb with extra characters
    }
}
