/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms;

import java.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
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
	 */
	public static final String TITLE = "org.eclipse.ui.forms.TITLE";

	/**
	 * Key for the tree/table border color.
	 */
	public static final String BORDER = "org.eclipse.ui.forms.BORDER";

	/**
	 * Key for the section separator color.
	 */
	public static final String SEPARATOR = "org.eclipse.ui.forms.SEPARATOR";

	/**
	 * Key for the section title bar background.
	 */
	public static final String TB_BG = "org.eclipse.ui.forms.TB_BG";

	/**
	 * Key for the section title bar foreground.
	 * 
	 * @deprecated this color is not used
	 */
	public static final String TB_FG = "org.eclipse.ui.forms.TB_FG";

	/**
	 * Key for the section title bar gradient.
	 */
	public static final String TB_GBG = "org.eclipse.ui.forms.TB_GBG";

	/**
	 * Key for the section title bar border.
	 */
	public static final String TB_BORDER = "org.eclipse.ui.forms.TB_BORDER";

	/**
	 * Key for the section toggle color.
	 */
	public static final String TB_TOGGLE = "org.eclipse.ui.forms.TB_TOGGLE";

	protected Map colorRegistry = new HashMap(10);

	protected Color background;

	protected Color foreground;

	private boolean shared;

	protected Display display;

	protected Color border;

	private static final RGB white = new RGB(255, 255, 255);

	private static final RGB black = new RGB(0, 0, 0);

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
	 * Allocates colors for the following keys: BORDER, COMPOSITE_SEPARATOR and
	 * DEFAULT_HEADER. Subclasses can override to allocate this colors
	 * differently.
	 */
	protected void initializeColorTable() {
		createColor(SEPARATOR, 152, 170, 203);
		createTitleColor();
		RGB border = getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
		RGB black = new RGB(0, 0, 0);
		createColor(BORDER, blend(border, black, 80));
	}

	private void createTitleColor() {
		/*
		 * String osname = System.getProperty("os.name").toLowerCase(); if
		 * (osname.startsWith("mac os")) createColor(TITLE,
		 * getSystemColor(SWT.COLOR_LIST_FOREGROUND)); else createColor(TITLE,
		 * getSystemColor(SWT.COLOR_LIST_SELECTION));
		 */
		RGB rgb = getSystemColor(SWT.COLOR_LIST_SELECTION);
		RGB white = new RGB(255, 255, 255);
		RGB black = new RGB(0, 0, 0);
		// test too light
		if (testTwoPrimaryColors(rgb, 120, 151))
			rgb = blend(rgb, black, 80);
		else if (testTwoPrimaryColors(rgb, 150, 256))
			rgb = blend(rgb, black, 50);
		createColor(TITLE, rgb);
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
		if (getColor(FormColors.TB_BG) != null)
			return;

		createTitleBarGradient();
		createTitleBarOutline();
		// toggle color
		RGB toggle = getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		createColor(FormColors.TB_TOGGLE, toggle);
	}
	
	private void createTitleBarGradient() {
		RGB tbBg = getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
		Color bg = getImpliedBackground();
		RGB formBackground = bg.getRGB();
		
		// blend 77% white with the title background gradient
		tbBg = blend(formBackground, tbBg, 77);
	
		if (isWhiteBackground()) {
			// corrections
			if (testTwoPrimaryColors(tbBg, 241, 256)) {
				// too light
				tbBg = blend(tbBg, black, 90);
			}
			else if (testTwoPrimaryColors(tbBg, 0, 231)) {
				// too dark
				if (testAnyPrimaryColor(tbBg, 214, 231))
					tbBg = blend(tbBg, white, 95);
				else if (testAnyPrimaryColor(tbBg, 199, 215))
					tbBg = blend(tbBg, white, 90);
			}
		}
		else {
			if (testTwoPrimaryColors(tbBg, 209, 256)) {
				// too light
				if (testAnyPrimaryColor(tbBg, 210, 236))
					tbBg = blend(tbBg, black, 60);
				else if (testAnyPrimaryColor(tbBg, 235, 256))
					tbBg = blend(tbBg, black, 20);
			}
		}
		createColor(FormColors.TB_BG, tbBg);		

		// blend 50 % white with the previous blend for half-way
		RGB tbGbg = blend(formBackground, tbBg, 50);
		createColor(FormColors.TB_GBG, tbGbg);
	}
	
	private void createTitleBarOutline() {
		// title bar outline - border color
		RGB tbBorder = getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
		// Perform adjustments for too bright
		if (isWhiteBackground()) {
			if (testTwoPrimaryColors(tbBorder, 215, 256)) {
				// too bright
				if (testAnyPrimaryColor(tbBorder, 215, 226))
					// 	decrease white by 10%
					tbBorder = blend(tbBorder, black, 90);
				else if (testAnyPrimaryColor(tbBorder, 225, 256))
					// decrease white by 20%
					tbBorder = blend(tbBorder, black, 70);
			}
			else if (testTwoPrimaryColors(tbBorder, 0, 186)) {
				// too dark
				if (testAnyPrimaryColor(tbBorder, 175, 186))
					// add 5% white
					tbBorder = blend(tbBorder, white, 95);
				else if (testTwoPrimaryColors(tbBorder, 154, 176))
					// add 10% white
					tbBorder = blend(tbBorder, white, 90);
				else if (testTwoPrimaryColors(tbBorder, 124, 155))
					// add 20% white
					tbBorder = blend(tbBorder, white, 80);
			}
		} else {
			if (testTwoPrimaryColors(tbBorder, 200, 256))
				// too bright - decrease white by 50%
				tbBorder = blend(tbBorder, black, 50);
		}
		createColor(FormColors.TB_BORDER, tbBorder);		
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
		return createColor(key, rgb.red, rgb.green, rgb.blue);
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
		String key = "__ncbg__";
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
		Color c = new Color(display, r, g, b);
		Color prevC = (Color) colorRegistry.get(key);
		if (prevC != null)
			prevC.dispose();
		colorRegistry.put(key, c);
		return c;
	}

	/**
	 * Computes the border color relative to the background. Allocated border
	 * color is designed to work well with white. Otherwise, stanard widget
	 * background color will be used.
	 */
	protected void updateBorderColor() {
		if (isWhiteBackground())
			border = getColor(BORDER);
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
	 * @return <samp>true </samp> if background is white, <samp>false </samp>
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
		return (Color) colorRegistry.get(key);
	}

	/**
	 * Disposes all the colors in the registry.
	 */
	public void dispose() {
		Iterator e = colorRegistry.values().iterator();
		while (e.hasNext())
			((Color) e.next()).dispose();
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
	 * Blends c1 and c2 based on the provided ratio.
	 * 
	 * @param c1
	 *            first color
	 * @param c2
	 *            second color
	 * @param ratio
	 *            percentage of the first color in the blend
	 * @return the RGB value of the blended color
	 */
	private RGB blend(RGB c1, RGB c2, int ratio) {
		int r = blend(c1.red, c2.red, ratio);
		int g = blend(c1.green, c2.green, ratio);
		int b = blend(c1.blue, c2.blue, ratio);
		return new RGB(r, g, b);
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
	private int blend(int v1, int v2, int ratio) {
		int b = (ratio * v1 + (100 - ratio) * v2) / 100;
		return Math.min(255, b);
	}

	private Color getImpliedBackground() {
		if (getBackground() != null)
			return getBackground();
		return getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	}

	private boolean testAnyPrimaryColor(RGB rgb, int from, int to) {
		if (testPrimaryColor(rgb.red, from, to))
			return true;
		if (testPrimaryColor(rgb.green, from, to))
			return true;
		if (testPrimaryColor(rgb.blue, from, to))
			return true;
		return false;
	}

	private boolean testTwoPrimaryColors(RGB rgb, int from, int to) {
		int total = 0;
		if (testPrimaryColor(rgb.red, from, to))
			total++;
		if (testPrimaryColor(rgb.green, from, to))
			total++;
		if (testPrimaryColor(rgb.blue, from, to))
			total++;
		return total >= 2;
	}

	private boolean testPrimaryColor(int value, int from, int to) {
		return value > from && value < to;
	}
}
