/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;


/**
 * Description of textual attributes such as color and style. Text attributes
 * are considered value objects.
 * <p>
 * Clients usually instantiate object of the class.</p>
 */
public class TextAttribute {

	/**
	 * Text attribute for strikethrough style.
	 * (value <code>1 << 29</code>).
	 * @since 3.1
	 */
	public static final int STRIKETHROUGH= 1 << 29;

	/**
	 * Text attribute for underline style.
	 * (value <code>1 << 30</code>)
	 * @since 3.1
	 */
	public static final int UNDERLINE= 1 << 30;


	/** Foreground color */
	private Color foreground;

	/** Background color */
	private Color background;

	/** The text style */
	private int style;

	/**
	 * Creates a text attribute with the given colors and style.
	 *
	 * @param foreground the foreground color
	 * @param background the background color, <code>null</code> if none
	 * @param style the style
	 */
	public TextAttribute(Color foreground, Color background, int style) {
		this.foreground= foreground;
		this.background= background;
		this.style= style;
	}

	/**
	 * Creates a text attribute for the given foreground color, no background color and
	 * with the SWT normal style.
	 *
	 * @param foreground the foreground color
	 */
	public TextAttribute(Color foreground) {
		this(foreground, null, SWT.NORMAL);
	}

	/*
	 * @see Object#equals(Object)
	 */
	public boolean equals(Object object) {

		if (object == this)
			return true;

		if (!(object instanceof TextAttribute))
			return false;

		TextAttribute a= (TextAttribute) object;
		return (a.style == style && equals(a.foreground, foreground) && equals(a.background, background));
	}

	/**
	 * Returns whether the two given objects are equal.
	 *
	 * @param o1 the first object, can be <code>null</code>
	 * @param o2 the second object, can be <code>null</code>
	 * @return <code>true</code> if the given objects are equals
	 * @since 2.0
	 */
	private boolean equals(Object o1, Object o2) {
		if (o1 != null)
			return o1.equals(o2);
		return (o2 == null);
	}

	/*
	 * @see Object#hashCode()
	 */
	 public int hashCode() {
	 	int foregroundHash= foreground == null ? 0 : foreground.hashCode();
	 	int backgroundHash= background == null ? 0 : background.hashCode();
	 	return (foregroundHash << 24) | (backgroundHash << 16) | style;
	 }

	/**
	 * Returns the attribute's foreground color.
	 *
	 * @return the attribute's foreground color
	 */
	public Color getForeground() {
		return foreground;
	}

	/**
	 * Returns the attribute's background color.
	 *
	 * @return the attribute's background color
	 */
	public Color getBackground() {
		return background;
	}

	/**
	 * Returns the attribute's style.
	 *
	 * @return the attribute's style
	 */
	public int getStyle() {
		return style;
	}
}
