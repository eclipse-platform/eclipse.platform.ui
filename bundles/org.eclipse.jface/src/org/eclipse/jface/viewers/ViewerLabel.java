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
package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * The ViewerLabel is the class that is passed to a viewer to handle updates of
 * labels. It keeps track of both original and updates text.
 * 
 * @see IViewerLabelProvider
 * @since 3.0
 */
public class ViewerLabel {

	//New values for the receiver. Null if nothing has been set.
	private String newText = null;

	private Image newImage = null;

	private Color background = null;

	private Color foreground = null;

	private Font font = null;

	//The initial values for the receiver.
	private String startText;

	private Image startImage;

	/**
	 * Create a new instance of the receiver with the supplied
	 * initial text and image.
	 * @param initialText
	 * @param initialImage
	 */
	public ViewerLabel(String initialText, Image initialImage) {
		startText = initialText;
		startImage = initialImage;
	}

	/**
	 * Get the image for the receiver. If the new image has been set return it,
	 * otherwise return the starting image.
	 * 
	 * @return Returns the image.
	 */
	public final Image getImage() {
		if (newImage == null)
			return startImage;
		return newImage;
	}

	/**
	 * Set the image for the receiver.
	 * 
	 * @param image
	 *            The image to set.
	 */
	public final void setImage(Image image) {
		newImage = image;
	}

	/**
	 * Get the text for the receiver. If the new text has been set return it,
	 * otherwise return the starting text.
	 * 
	 * @return Returns the text.
	 */
	public final String getText() {
		if (newText == null)
			return startText;
		return newText;
	}

	/**
	 * Set the text for the receiver.
	 * 
	 * @param text
	 *            The label to set.
	 */
	public final void setText(String text) {
		newText = text;
	}

	/**
	 * Return whether or not the image has been set.
	 * 
	 * @return boolean. <code>true</code>  if the image has been set to something new.
	 */
	public boolean hasNewImage() {

		//If we started with null any change is an update
		if (startImage == null)
			return newImage != null;

		return !(startImage.equals(newImage));
	}

	/**
	 * Return whether or not the text has been set.
	 * 
	 * @return boolean. <code>true</code>  if the text has been set to something new.
	 */
	public boolean hasNewText() {

		//If we started with null any change is an update
		if (startText == null)
			return newText != null;

		return !(startText.equals(newText));
	}

	/**
	 * Return whether or not the background color has been set.
	 * 
	 * @return boolean. <code>true</code> if the value has been set.
	 */
	public boolean hasNewBackground() {
		return background != null;
	}
	
	/**
	 * Return whether or not the foreground color has been set.
	 * 
	 * @return boolean. <code>true</code> if the value has been set.
	 */
	public boolean hasNewForeground() {
		return foreground != null;
	}
	
	/**
	 * Return whether or not the font has been set.
	 * 
	 * @return boolean. <code>true</code> if the value has been set.
	 */
	public boolean hasNewFont() {
		return font != null;
	}

	/**
	 * Get the background Color.
	 * @return Color or <code>null</code> if no new value
	 * was set.
	 */
	public Color getBackground() {
		return background;
	}
	/**
	 * Set the background Color.
	 * @param background Color
	 */
	public void setBackground(Color background) {
		this.background = background;
	}
	/**
	 * Get the font.
	 * @return Font or <code>null</code> if no new value
	 * was set.
	 */
	public Font getFont() {
		return font;
	}
	/**
	 * Set the font.
	 * @param font Font
	 */
	public void setFont(Font font) {
		this.font = font;
	}
	/**
	 * Get the foreground Color.
	 * @return Color or <code>null</code> if no new value
	 * was set.
	 */
	public Color getForeground() {
		return foreground;
	}
	/**
	 * Set the foreground Color.
	 * @param foreground
	 */
	public void setForeground(Color foreground) {
		this.foreground = foreground;
	}
}