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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Manages colors that will be applied to forms and form widgets. The colors
 * are chosen to make the widgets look correct in the editor area. If a
 * different set of colors is needed, subclass this class and override
 * 'initialize' and/or 'initializeColors'.
 */
public class FormColors {
	public static final String TITLE = "org.eclipse.ui.forms.TITLE";
	public static final String BORDER = "org.eclipse.ui.forms.BORDER";
	public static final String SEPARATOR = "org.eclipse.ui.forms.SEPARATOR";

	protected Map colorRegistry = new HashMap(10);

	private Color background;
	private Color foreground;
	private boolean shared;
	private Display display;
	protected Color border;

	public FormColors(Display display) {
		this.display = display;
		initialize();
	}
	
	public Display getDisplay() {
		return display;
	}

	protected void initialize() {
		background = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		foreground = display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
		initializeColors();
		updateBorderColor();
	}

	/**
	 * Allocates colors for the following keys: BORDER, COMPOSITE_SEPARATOR and
	 * DEFAULT_HEADER. Subclasses can override to allocate this colors
	 * differently.
	 */
	protected void initializeColors() {
		createColor(BORDER, 195, 191, 179);
		createColor(SEPARATOR, 152, 170, 203);
		createColor(TITLE, 0x48, 0x70, 0x98);
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

	public void setBackground(Color bg) {
		this.background = bg;
		updateBorderColor();
	}

	public void setForeground(Color fg) {
		this.foreground = fg;
	}

	public Color getBackground() {
		return background;
	}

	public Color getForeground() {
		return foreground;
	}

	public Color getBorderColor() {
		return border;
	}
/**
 * Tests if the background is white. White background has RGB value
 * 255,255,255.
 * @return <samp>true</samp> if background is white, <samp>false</samp>
 * otherwise.
 */
	public boolean isWhiteBackground() {
		return background.getRed() == 255
			&& background.getGreen() == 255
			&& background.getBlue() == 255;
	}

/**
 * Returns the color object for the provided key or <samp>null</samp>
 * if not in the registry.
 * @param key the color key
 * @return color object if found, or <samp>null</samp> if not.
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
	 * Marks the colors shared. Shared colors should not be disposed
	 * by individual clients that use them.
	 */
	public void markShared() {
		this.shared = true;
	}
	/**
	 * Tests if the colors are shared.
	 * @return <code>true</code> if shared, <code>false</code> otherwise.
	 */
	public boolean isShared() {
		return shared;
	}
}