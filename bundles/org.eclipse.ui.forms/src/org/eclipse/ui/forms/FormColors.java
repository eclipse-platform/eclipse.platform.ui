/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Manages colors that will be applied to forms and form widgets. The colors are
 * chosen to make the widgets look correct in the editor area. If a different
 * set of colors is needed, subclass this class and override 'initialize' and/or
 * 'initializeColors'.
 * 
 * @since 3.0
 */
public class FormColors {
	/**
	 * Key for the form title foreground color.
	 * 
	 * @deprecated use <code>IFormColors.TITLE</code>.
	 */
	public static final String TITLE = IFormColors.TITLE;

	/**
	 * Key for the tree/table border color.
	 * 
	 * @deprecated use <code>IFormColors.BORDER</code>
	 */
	public static final String BORDER = IFormColors.BORDER;

	/**
	 * Key for the section separator color.
	 * 
	 * @deprecated use <code>IFormColors.SEPARATOR</code>.
	 */
	public static final String SEPARATOR = IFormColors.SEPARATOR;

	/**
	 * Key for the section title bar background.
	 * 
	 * @deprecated use <code>IFormColors.TB_BG
	 */
	public static final String TB_BG = IFormColors.TB_BG;

	/**
	 * Key for the section title bar foreground.
	 * 
	 * @deprecated use <code>IFormColors.TB_FG</code>
	 */
	public static final String TB_FG = IFormColors.TB_FG;

	/**
	 * Key for the section title bar gradient.
	 * 
	 * @deprecated use <code>IFormColors.TB_GBG</code>
	 */
	public static final String TB_GBG = IFormColors.TB_GBG;

	/**
	 * Key for the section title bar border.
	 * 
	 * @deprecated use <code>IFormColors.TB_BORDER</code>.
	 */
	public static final String TB_BORDER = IFormColors.TB_BORDER;

	/**
	 * Key for the section toggle color. Since 3.1, this color is used for all
	 * section styles.
	 * 
	 * @deprecated use <code>IFormColors.TB_TOGGLE</code>.
	 */
	public static final String TB_TOGGLE = IFormColors.TB_TOGGLE;

	/**
	 * Key for the section toggle hover color.
	 * 
	 * @since 3.1
	 * @deprecated use <code>IFormColors.TB_TOGGLE_HOVER</code>.
	 */
	public static final String TB_TOGGLE_HOVER = IFormColors.TB_TOGGLE_HOVER;

	protected Map colorRegistry = new HashMap(10);
	
	private LocalResourceManager resources;

	protected Color background;

	protected Color foreground;

	private boolean shared;

	protected Display display;

	protected Color border;

	/**
	 * Creates form colors using the provided display.
	 * 
	 * @param display
	 *            the display to use
	 */
	public FormColors(Display display) {
		this.display = display;
		initialize();
	}

	/**
	 * Returns the display used to create colors.
	 * 
	 * @return the display
	 */
	public Display getDisplay() {
		return display;
	}

	/**
	 * Initializes the colors. Subclasses can override this method to change the
	 * way colors are created. Alternatively, only the color table can be
	 * modified by overriding <code>initializeColorTable()</code>.
	 * 
	 * @see #initializeColorTable
	 */
	protected void initialize() {
		background = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		foreground = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		initializeColorTable();
		updateBorderColor();
	}

	/**
	 * Allocates colors for the following keys: BORDER, SEPARATOR and
	 * TITLE. Subclasses can override to allocate these colors differently.
	 */
	protected void initializeColorTable() {
		createTitleColor();
		createColor(IFormColors.SEPARATOR, getColor(IFormColors.TITLE).getRGB());
		RGB black = getSystemColor(SWT.COLOR_BLACK);
		RGB borderRGB = getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
		createColor(IFormColors.BORDER, blend(borderRGB, black, 80));
	}

	/**
	 * Allocates colors for the section tool bar (all the keys that start with
	 * TB). Since these colors are only needed when TITLE_BAR style is used with
	 * the Section widget, they are not needed all the time and are allocated on
	 * demand. Consequently, this method will do nothing if the colors have been
	 * already initialized. Call this method prior to using colors with the TB
	 * keys to ensure they are available.
	 */
	public void initializeSectionToolBarColors() {
		if (colorRegistry.containsKey(IFormColors.TB_BG))
			return;
		createTitleBarGradientColors();
		createTitleBarOutlineColors();
		createTwistieColors();
	}

	/**
	 * Allocates additional colors for the form header, namely background
	 * gradients, bottom separator keylines and DND highlights. Since these
	 * colors are only needed for clients that want to use these particular
	 * style of header rendering, they are not needed all the time and are
	 * allocated on demand. Consequently, this method will do nothing if the
	 * colors have been already initialized. Call this method prior to using
	 * color keys with the H_ prefix to ensure they are available.
	 * 
	 * @since 3.3
	 */
	protected void initializeFormHeaderColors() {
		if (colorRegistry.containsKey(IFormColors.H_BOTTOM_KEYLINE2))
			return;
		createFormHeaderColors();
	}

	/**
	 * Returns the RGB value of the system color represented by the code
	 * argument, as defined in <code>SWT</code> class.
	 * 
	 * @param code
	 *            the system color constant as defined in <code>SWT</code>
	 *            class.
	 * @return the RGB value of the system color
	 */
	public RGB getSystemColor(int code) {
		return getDisplay().getSystemColor(code).getRGB();
	}

	/**
	 * Creates the color for the specified key using the provided RGB object.
	 * The color object will be returned and also put into the registry. When
	 * the class is disposed, the color will be disposed with it.
	 * 
	 * @param key
	 *            the unique color key
	 * @param rgb
	 *            the RGB object
	 * @return the allocated color object
	 */
	public Color createColor(String key, RGB rgb) {
		Color c = getResourceManager().createColor(rgb);
		Color prevC = (Color) colorRegistry.get(key);
		if (prevC != null && !prevC.isDisposed())
			getResourceManager().destroyColor(prevC.getRGB());
		colorRegistry.put(key, c);
		return c;
	}

	/**
	 * Creates a color that can be used for areas of the form that is inactive.
	 * These areas can contain images, links, controls and other content but are
	 * considered auxilliary to the main content area.
	 * 
	 * <p>
	 * The color should not be disposed because it is managed by this class.
	 * 
	 * @return the inactive form color
	 * @since 3.1
	 */
	public Color getInactiveBackground() {
		String key = "__ncbg__"; //$NON-NLS-1$
		Color color = getColor(key);
		if (color == null) {
			RGB sel = getSystemColor(SWT.COLOR_LIST_SELECTION);
			// a blend of 95% white and 5% list selection system color
			RGB ncbg = blend(sel, getSystemColor(SWT.COLOR_WHITE), 5);
			color = createColor(key, ncbg);
		}
		return color;
	}

	/**
	 * Creates the color for the specified key using the provided RGB values.
	 * The color object will be returned and also put into the registry. If
	 * there is already another color object under the same key in the registry,
	 * the existing object will be disposed. When the class is disposed, the
	 * color will be disposed with it.
	 * 
	 * @param key
	 *            the unique color key
	 * @param r
	 *            red value
	 * @param g
	 *            green value
	 * @param b
	 *            blue value
	 * @return the allocated color object
	 */
	public Color createColor(String key, int r, int g, int b) {
		return createColor(key, new RGB(r,g,b));
	}

	/**
	 * Computes the border color relative to the background. Allocated border
	 * color is designed to work well with white. Otherwise, stanard widget
	 * background color will be used.
	 */
	protected void updateBorderColor() {
		if (isWhiteBackground())
			border = getColor(IFormColors.BORDER);
		else {
			border = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
			Color bg = getImpliedBackground();
			if (border.getRed() == bg.getRed()
					&& border.getGreen() == bg.getGreen()
					&& border.getBlue() == bg.getBlue())
				border = display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
		}
	}

	/**
	 * Sets the background color. All the toolkits that use this class will
	 * share the same background.
	 * 
	 * @param bg
	 *            background color
	 */
	public void setBackground(Color bg) {
		this.background = bg;
		updateBorderColor();
		updateFormHeaderColors();
	}

	/**
	 * Sets the foreground color. All the toolkits that use this class will
	 * share the same foreground.
	 * 
	 * @param fg
	 *            foreground color
	 */
	public void setForeground(Color fg) {
		this.foreground = fg;
	}

	/**
	 * Returns the current background color.
	 * 
	 * @return the background color
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * Returns the current foreground color.
	 * 
	 * @return the foreground color
	 */
	public Color getForeground() {
		return foreground;
	}

	/**
	 * Returns the computed border color. Border color depends on the background
	 * and is recomputed whenever the background changes.
	 * 
	 * @return the current border color
	 */
	public Color getBorderColor() {
		return border;
	}

	/**
	 * Tests if the background is white. White background has RGB value
	 * 255,255,255.
	 * 
	 * @return <samp>true</samp> if background is white, <samp>false</samp>
	 *         otherwise.
	 */
	public boolean isWhiteBackground() {
		Color bg = getImpliedBackground();
		return bg.getRed() == 255 && bg.getGreen() == 255
				&& bg.getBlue() == 255;
	}

	/**
	 * Returns the color object for the provided key or <samp>null </samp> if
	 * not in the registry.
	 * 
	 * @param key
	 *            the color key
	 * @return color object if found, or <samp>null </samp> if not.
	 */
	public Color getColor(String key) {
		if (key.startsWith(IFormColors.TB_PREFIX))
			initializeSectionToolBarColors();
		else if (key.startsWith(IFormColors.H_PREFIX))
			initializeFormHeaderColors();
		return (Color) colorRegistry.get(key);
	}

	/**
	 * Disposes all the colors in the registry.
	 */
	public void dispose() {
		if (resources != null)
			resources.dispose();
		resources = null;
		colorRegistry = null;
	}

	/**
	 * Marks the colors shared. This prevents toolkits that share this object
	 * from disposing it.
	 */
	public void markShared() {
		this.shared = true;
	}

	/**
	 * Tests if the colors are shared.
	 * 
	 * @return <code>true</code> if shared, <code>false</code> otherwise.
	 */
	public boolean isShared() {
		return shared;
	}

	/**
	 * Blends c1 and c2 based in the provided ratio.
	 * 
	 * @param c1
	 *            first color
	 * @param c2
	 *            second color
	 * @param ratio
	 *            percentage of the first color in the blend (0-100)
	 * @return the RGB value of the blended color
	 * @since 3.1
	 */
	public static RGB blend(RGB c1, RGB c2, int ratio) {
		int r = blend(c1.red, c2.red, ratio);
		int g = blend(c1.green, c2.green, ratio);
		int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
	}

	/**
	 * Tests the source RGB for range.
	 * 
	 * @param rgb
	 *            the tested RGB
	 * @param from
	 *            range start (excluding the value itself)
	 * @param to
	 *            range end (excluding the value itself)
	 * @return <code>true</code> if at least one of the primary colors in the
	 *         source RGB are within the provided range, <code>false</code>
	 *         otherwise.
	 * @since 3.1
	 */
	public static boolean testAnyPrimaryColor(RGB rgb, int from, int to) {
		if (testPrimaryColor(rgb.red, from, to))
			return true;
		if (testPrimaryColor(rgb.green, from, to))
			return true;
		if (testPrimaryColor(rgb.blue, from, to))
			return true;
		return false;
	}

	/**
	 * Tests the source RGB for range.
	 * 
	 * @param rgb
	 *            the tested RGB
	 * @param from
	 *            range start (excluding the value itself)
	 * @param to
	 *            tange end (excluding the value itself)
	 * @return <code>true</code> if at least two of the primary colors in the
	 *         source RGB are within the provided range, <code>false</code>
	 *         otherwise.
	 * @since 3.1
	 */
	public static boolean testTwoPrimaryColors(RGB rgb, int from, int to) {
		int total = 0;
		if (testPrimaryColor(rgb.red, from, to))
			total++;
		if (testPrimaryColor(rgb.green, from, to))
			total++;
		if (testPrimaryColor(rgb.blue, from, to))
			total++;
		return total >= 2;
	}

	/**
	 * Blends two primary color components based on the provided ratio.
	 * 
	 * @param v1
	 *            first component
	 * @param v2
	 *            second component
	 * @param ratio
	 *            percentage of the first component in the blend
	 * @return
	 */
	private static int blend(int v1, int v2, int ratio) {
		int b = (ratio * v1 + (100 - ratio) * v2) / 100;
		return Math.min(255, b);
	}

	private Color getImpliedBackground() {
		if (getBackground() != null)
			return getBackground();
		return getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	}

	private static boolean testPrimaryColor(int value, int from, int to) {
		return value > from && value < to;
	}

	private void createTitleColor() {
		/*
		 * RGB rgb = getSystemColor(SWT.COLOR_LIST_SELECTION); // test too light
		 * if (testTwoPrimaryColors(rgb, 120, 151)) rgb = blend(rgb, BLACK, 80);
		 * else if (testTwoPrimaryColors(rgb, 150, 256)) rgb = blend(rgb, BLACK,
		 * 50); createColor(TITLE, rgb);
		 */
		RGB bg = getImpliedBackground().getRGB();
		RGB listSelection = getSystemColor(SWT.COLOR_LIST_SELECTION);
		RGB listForeground = getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		RGB rgb = listSelection;

		// Group 1
		// Rule: If at least 2 of the LIST_SELECTION RGB values are equal to or
		// between 0 and 120, then use 100% LIST_SELECTION as it is (no
		// additions)
		// Examples: XP Default, Win Classic Standard, Win High Con White, Win
		// Classic Marine
		if (testTwoPrimaryColors(listSelection, -1, 121))
			rgb = listSelection;
		// Group 2
		// When LIST_BACKGROUND = white (255, 255, 255) or not black, text
		// colour = LIST_SELECTION @ 100% Opacity + 50% LIST_FOREGROUND over
		// LIST_BACKGROUND
		// Rule: If at least 2 of the LIST_SELECTION RGB values are equal to or
		// between 121 and 255, then add 50% LIST_FOREGROUND to LIST_SELECTION
		// foreground colour
		// Examples: Win Vista, XP Silver, XP Olive , Win Classic Plum, OSX
		// Aqua, OSX Graphite, Linux GTK
		else if (testTwoPrimaryColors(listSelection, 120, 256)
				|| (bg.red == 0 && bg.green == 0 && bg.blue == 0))
			rgb = blend(listSelection, listForeground, 50);
		// Group 3
		// When LIST_BACKGROUND = black (0, 0, 0), text colour = LIST_SELECTION
		// @ 100% Opacity + 50% LIST_FOREGROUND over LIST_BACKGROUND
		// Rule: If LIST_BACKGROUND = 0, 0, 0, then add 50% LIST_FOREGROUND to
		// LIST_SELECTION foreground colour
		// Examples: Win High Con Black, Win High Con #1, Win High Con #2
		// (covered in the second part of the OR clause above)
		createColor(IFormColors.TITLE, rgb);
	}

	private void createTwistieColors() {
		RGB rgb = getColor(IFormColors.TITLE).getRGB();
		RGB white = getSystemColor(SWT.COLOR_WHITE);
		createColor(TB_TOGGLE, rgb);
		rgb = blend(rgb, white, 60);
		createColor(TB_TOGGLE_HOVER, rgb);
	}

	private void createTitleBarGradientColors() {
		RGB tbBg = getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
		RGB bg = getImpliedBackground().getRGB();

		// Group 1
		// Rule: If at least 2 of the RGB values are equal to or between 180 and
		// 255, then apply specified opacity for Group 1
		// Examples: Vista, XP Silver, Wn High Con #2
		// Gradient Bottom = TITLE_BACKGROUND @ 30% Opacity over LIST_BACKGROUND
		// Gradient Top = TITLE BACKGROUND @ 0% Opacity over LIST_BACKGROUND
		if (testTwoPrimaryColors(tbBg, 179, 256))
			tbBg = blend(tbBg, bg, 30);

		// Group 2
		// Rule: If at least 2 of the RGB values are equal to or between 121 and
		// 179, then apply specified opacity for Group 2
		// Examples: XP Olive, OSX Graphite, Linux GTK, Wn High Con Black
		// Gradient Bottom = TITLE_BACKGROUND @ 20% Opacity over LIST_BACKGROUND
		// Gradient Top = TITLE BACKGROUND @ 0% Opacity over LIST_BACKGROUND
		else if (testTwoPrimaryColors(tbBg, 120, 180))
			tbBg = blend(tbBg, bg, 20);

		// Group 3
		// Rule: Everything else
		// Examples: XP Default, Wn Classic Standard, Wn Marine, Wn Plum, OSX
		// Aqua, Wn High Con White, Wn High Con #1
		// Gradient Bottom = TITLE_BACKGROUND @ 10% Opacity over LIST_BACKGROUND
		// Gradient Top = TITLE BACKGROUND @ 0% Opacity over LIST_BACKGROUND
		else {
			tbBg = blend(tbBg, bg, 10);
		}

		createColor(IFormColors.TB_BG, tbBg);
		
		// for backward compatibility
		createColor(TB_GBG, tbBg);
	}

	private void createTitleBarOutlineColors() {
		// title bar outline - border color
		RGB tbBorder = getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
		RGB bg = getImpliedBackground().getRGB();
		// Group 1
		// Rule: If at least 2 of the RGB values are equal to or between 180 and
		// 255, then apply specified opacity for Group 1
		// Examples: Vista, XP Silver, Wn High Con #2
		// Keyline = TITLE_BACKGROUND @ 70% Opacity over LIST_BACKGROUND
		if (testTwoPrimaryColors(tbBorder, 179, 256))
			tbBorder = blend(tbBorder, bg, 70);

		// Group 2
		// Rule: If at least 2 of the RGB values are equal to or between 121 and
		// 179, then apply specified opacity for Group 2
		// Examples: XP Olive, OSX Graphite, Linux GTK, Wn High Con Black

		// Keyline = TITLE_BACKGROUND @ 50% Opacity over LIST_BACKGROUND
		else if (testTwoPrimaryColors(tbBorder, 120, 180))
			tbBorder = blend(tbBorder, bg, 50);

		// Group 3
		// Rule: Everything else
		// Examples: XP Default, Wn Classic Standard, Wn Marine, Wn Plum, OSX
		// Aqua, Wn High Con White, Wn High Con #1

		// Keyline = TITLE_BACKGROUND @ 30% Opacity over LIST_BACKGROUND
		else {
			tbBorder = blend(tbBorder, bg, 30);
		}
		createColor(FormColors.TB_BORDER, tbBorder);
	}

	private void updateFormHeaderColors() {
		if (colorRegistry.containsKey(IFormColors.H_GRADIENT_END)) {
			disposeIfFound(IFormColors.H_GRADIENT_END);
			disposeIfFound(IFormColors.H_GRADIENT_START);
			disposeIfFound(IFormColors.H_BOTTOM_KEYLINE1);
			disposeIfFound(IFormColors.H_BOTTOM_KEYLINE2);
			disposeIfFound(IFormColors.H_HOVER_LIGHT);
			disposeIfFound(IFormColors.H_HOVER_FULL);
			initializeFormHeaderColors();
		}
	}

	private void disposeIfFound(String key) {
		Color color = getColor(key);
		if (color != null) {
			colorRegistry.remove(key);
			color.dispose();
		}
	}

	private void createFormHeaderColors() {
		createFormHeaderGradientColors();
		createFormHeaderKeylineColors();
		createFormHeaderDNDColors();
	}

	private void createFormHeaderGradientColors() {
		RGB titleBg = getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
		Color bgColor = getImpliedBackground();
		RGB bg = bgColor.getRGB();
		RGB bottom, top;
		// Group 1
		// Rule: If at least 2 of the RGB values are equal to or between 180 and
		// 255, then apply specified opacity for Group 1
		// Examples: Vista, XP Silver, Wn High Con #2
		// Gradient Bottom = TITLE_BACKGROUND @ 30% Opacity over LIST_BACKGROUND
		// Gradient Top = TITLE BACKGROUND @ 0% Opacity over LIST_BACKGROUND
		if (testTwoPrimaryColors(titleBg, 179, 256)) {
			bottom = blend(titleBg, bg, 30);
			top = bg;
		}

		// Group 2
		// Rule: If at least 2 of the RGB values are equal to or between 121 and
		// 179, then apply specified opacity for Group 2
		// Examples: XP Olive, OSX Graphite, Linux GTK, Wn High Con Black
		// Gradient Bottom = TITLE_BACKGROUND @ 20% Opacity over LIST_BACKGROUND
		// Gradient Top = TITLE BACKGROUND @ 0% Opacity over LIST_BACKGROUND
		else if (testTwoPrimaryColors(titleBg, 120, 180)) {
			bottom = blend(titleBg, bg, 20);
			top = bg;
		}

		// Group 3
		// Rule: If at least 2 of the RGB values are equal to or between 0 and
		// 120, then apply specified opacity for Group 3
		// Examples: XP Default, Wn Classic Standard, Wn Marine, Wn Plum, OSX
		// Aqua, Wn High Con White, Wn High Con #1
		// Gradient Bottom = TITLE_BACKGROUND @ 10% Opacity over LIST_BACKGROUND
		// Gradient Top = TITLE BACKGROUND @ 0% Opacity over LIST_BACKGROUND
		else {
			bottom = blend(titleBg, bg, 10);
			top = bg;
		}
		createColor(IFormColors.H_GRADIENT_END, top);
		createColor(IFormColors.H_GRADIENT_START, bottom);
	}

	private void createFormHeaderKeylineColors() {
		RGB titleBg = getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
		Color bgColor = getImpliedBackground();
		RGB bg = bgColor.getRGB();
		RGB keyline2;
		// H_BOTTOM_KEYLINE1
		createColor(IFormColors.H_BOTTOM_KEYLINE1, new RGB(255, 255, 255));

		// H_BOTTOM_KEYLINE2
		// Group 1
		// Rule: If at least 2 of the RGB values are equal to or between 180 and
		// 255, then apply specified opacity for Group 1
		// Examples: Vista, XP Silver, Wn High Con #2
		// Keyline = TITLE_BACKGROUND @ 70% Opacity over LIST_BACKGROUND
		if (testTwoPrimaryColors(titleBg, 179, 256))
			keyline2 = blend(titleBg, bg, 70);

		// Group 2
		// Rule: If at least 2 of the RGB values are equal to or between 121 and
		// 179, then apply specified opacity for Group 2
		// Examples: XP Olive, OSX Graphite, Linux GTK, Wn High Con Black
		// Keyline = TITLE_BACKGROUND @ 50% Opacity over LIST_BACKGROUND
		else if (testTwoPrimaryColors(titleBg, 120, 180))
			keyline2 = blend(titleBg, bg, 50);

		// Group 3
		// Rule: If at least 2 of the RGB values are equal to or between 0 and
		// 120, then apply specified opacity for Group 3
		// Examples: XP Default, Wn Classic Standard, Wn Marine, Wn Plum, OSX
		// Aqua, Wn High Con White, Wn High Con #1

		// Keyline = TITLE_BACKGROUND @ 30% Opacity over LIST_BACKGROUND
		else
			keyline2 = blend(titleBg, bg, 30);
		// H_BOTTOM_KEYLINE2
		createColor(IFormColors.H_BOTTOM_KEYLINE2, keyline2);
	}

	private void createFormHeaderDNDColors() {
		RGB titleBg = getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
		Color bgColor = getImpliedBackground();
		RGB bg = bgColor.getRGB();
		RGB light, full;
		// ALL Themes
		//
		// Light Highlight
		// When *near* the 'hot' area
		// Rule: If near the title in the 'hot' area, show background highlight
		// TITLE_BACKGROUND_GRADIENT @ 40%
		light = blend(titleBg, bg, 40);
		// Full Highlight
		// When *on* the title area (regions 1 and 2)
		// Rule: If near the title in the 'hot' area, show background highlight
		// TITLE_BACKGROUND_GRADIENT @ 60%
		full = blend(titleBg, bg, 60);
		// H_DND_LIGHT
		// H_DND_FULL
		createColor(IFormColors.H_HOVER_LIGHT, light);
		createColor(IFormColors.H_HOVER_FULL, full);
	}
	
	private LocalResourceManager getResourceManager() {
		if (resources == null)
			resources = new LocalResourceManager(JFaceResources.getResources());
		return resources;
	}
}
