package org.eclipse.jface.text;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;


/**
 * Description of textual attributes such as color and style.
 * Text attributes are considered value objects.
 */
public class TextAttribute {
	
	/** Foreground color */
	Color foreground;
	
	/** Background color */
	Color background;
	
	/** The text style */
	int style;
	
	/**
	 * Creates a text attribute for the given foreground color, no background color and
	 * with the SWT normal style.
	 *
	 * @param foreground the foreground color
	 */
	public TextAttribute(Color foreground) {
		this(foreground, null, SWT.NORMAL);
	}
	/**
	 * Creates a text attribute with the given colors and style.
	 *
	 * @param foreground the foreground color
	 * @param background the background color
	 * @param style the style
	 */
	public TextAttribute(Color foreground, Color background, int style) {
		this.foreground= foreground;
		this.background= background;
		this.style= style;
	}
	/**
	 * 
	 * @see Object#equals
	 */
	public boolean equals(Object object) {
		
		if (object == this)
			return true;
		
		if (!(object instanceof TextAttribute))
			return false;
		
		TextAttribute a= (TextAttribute) object;			
		return (a.style == style && a.foreground == foreground && a.background == background);
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
	 * Returns the attribute's foreground color.
	 *
	 * @return the attribute's foreground color
	 */
	public Color getForeground() {
		return foreground;
	}
	/**
	 * Returns the attribute's style.
	 *
	 * @return the attribute's style
	 */
	public int getStyle() {
		return style;
	}
	/*
	 * @see Object#hashCode
	 */
	 public int hashCode() {
	 	int foregroundHash= foreground == null ? 0 : foreground.hashCode();
	 	int backgroundHash= background == null ? 0 : background.hashCode();
	 	return (foregroundHash << 24) | (backgroundHash << 16) | style;
	 }
}
