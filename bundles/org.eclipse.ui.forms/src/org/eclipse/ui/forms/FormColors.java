/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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
import org.eclipse.ui.forms.internal.widgets.FormUtil;
/**
 * Manages colors that will be applied to forms and form widgets. The colors
 * are chosen to make the widgets look correct in the editor area. If a
 * different set of colors is needed, subclass this class and override
 * 'initialize' and/or 'initializeColors'.
 * 
 * TODO (dejan) - spell out subclass contract
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

	public static final String TB_BG = "_sec_tb_bg";
	public static final String TB_FG = "_sec_tb_fg";
	public static final String TB_GBG = "_sec_tb_gbg";
	public static final String TB_BORDER = "_sec_tb_fg";
	public static final String TB_TOGGLE = "_sec_tb_twistie";	
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
		//createColor(BORDER, 195, 191, 179);
		createColor(SEPARATOR, 152, 170, 203);
		createColor(TITLE, FormUtil.getSystemColor(this, SWT.COLOR_LIST_SELECTION));
		createColor(BORDER, FormUtil.getSystemColor(this, SWT.COLOR_LIST_SELECTION));
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
		else
			border = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
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
		return background.getRed() == 255 && background.getGreen() == 255
				&& background.getBlue() == 255;
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
}