/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
 * Manages colors that will be applied to forms and form widgets. The colors
 * are chosen to make the widgets look correct in the editor area. If a
 * different set of colors is needed, subclass this class and override
 * 'initialize' and/or 'initializeColors'.
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
	 * Initializes the colors. Subclasses can override this method to change
	 * the way colors are created. Alternatively, only the color table can be
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
		String osname = System.getProperty("os.name").toLowerCase();
		if (osname.startsWith("mac os"))
			createColor(TITLE, getSystemColor(SWT.COLOR_LIST_FOREGROUND));
		else
			createColor(TITLE, getSystemColor(SWT.COLOR_LIST_SELECTION));
		RGB border = getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
		RGB black = new RGB(0,0,0);
		createColor(BORDER, blend(border, black, 80));
	}
/**
 * Allocates colors for the section tool bar (all the keys that
 * start with TB). Since these colors are only needed when
 * TITLE_BAR style is used with the Section widget, they
 * are not needed all the time and are allocated on demand.
 * Consequently, this method will do nothing if the colors
 * have been already initialized. Call this method prior to
 * using colors with the TB keys to ensure they are available.
 */	
	public void initializeSectionToolBarColors() {
		if (getColor(FormColors.TB_BG)!=null) return;
			
		RGB tbBg = getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
		Color bg = getImpliedBackground();
		RGB formBackground = bg.getRGB();
			
		// blend 77% white with the title background gradient
		tbBg = blend(formBackground, tbBg, 77);
		createColor(FormColors.TB_BG, tbBg);
		
		// blend 50 % white with the previous blend for half-way
		RGB tbGbg = blend(formBackground, tbBg, 50);
		createColor(FormColors.TB_GBG, tbGbg);
		
		// Title bar foreground
		RGB tbFg = getSystemColor(SWT.COLOR_LIST_SELECTION);
		createColor(FormColors.TB_FG, tbFg);
		
		// title bar outline - border color
		RGB tbBorder = getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
		createColor(FormColors.TB_BORDER, tbBorder);
		// toggle color
		RGB toggle = getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);
		createColor(FormColors.TB_TOGGLE, toggle);
	}
	/**
	 * Returns the RGB value of the system color represented by the code argument,
	 * as defined in <code>SWT</code> class.
	 * @param code the system color constant as defined in <code>SWT</code> class.
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
	 * Creates the color for the specified key using the provided RGB values.
	 * The color object will be returned and also put into the registry. When
	 * the class is disposed, the color will be disposed with it.
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
			if (border.getRed()==bg.getRed() &&
					border.getGreen()==bg.getGreen() &&
					border.getBlue()==bg.getBlue())
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
	 * Returns the computed border color. Border color depends on the
	 * background and is recomputed whenever the background changes.
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
	 * @param c1 first color
	 * @param c2 second color
	 * @param ratio percentage of the first color in the blend
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
	 * @param v1 first component
	 * @param v2 second component
	 * @param ratio percentage of the first component in the blend
	 * @return
	 */
	private int blend(int v1, int v2, int ratio) {
		return (ratio*v1 + (100-ratio)*v2)/100;
	}
	
	private Color getImpliedBackground() {
		if (getBackground()!=null)
			return getBackground();
		return getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	}
}
