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
	 * @param initialText the initial text for the label
	 * @param initialImage the initial image for the label
	 */
	public CombinedLabel(String initialText, Image initialImage) {
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
	 * @param newText the new text for the label
	 */
	public void setText(String newText) {
		text = newText;
	}

	/**
	 * Sets the label's image.
	 * 
	 * @param newImage the new image for the label
	 */
	public void setImage(Image newImage) {
		image = newImage;
	}
}