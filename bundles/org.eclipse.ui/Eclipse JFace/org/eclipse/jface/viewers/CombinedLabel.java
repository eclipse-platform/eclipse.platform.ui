package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.swt.graphics.Image;

/**
 * The CombinedLabel combines the text and image for the label of an element.
 */
public class CombinedLabel {

	private String text;
	private Image image;

	/**
	 * Create a new <code>CombinedLabel</code> with the given text and image.
	 * 
	 * @param text the text for the label
	 * @param image the image for the label
	 */
	public CombinedLabel(String text, Image image) {
		this.text = text;
		this.image = image;
	}

	/**
	 * Returns the label's text.
	 * 
	 * @return the label's text
	 */
	public String getText() {
		return text;
	}

	/**
	 * Returns the label's image.
	 * 
	 * @return the label's image
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Sets the label's text.
	 * 
	 * @param text the text for the label
	 */
	public void setText(String text) {
		this.text = text;
	}

	/**
	 * Sets the label's image.
	 * 
	 * @param image the image for the label
	 */
	public void setImage(Image image) {
		this.image = image;
	}
}