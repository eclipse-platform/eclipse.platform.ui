package org.eclipse.jface.viewers;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.swt.graphics.Image;

/**
 * The CombinedLabel is a type used to keep track of the result
 * of an Image and text decoration for an Object.
 * This is used by the ICombinedLabelDecorator to return the
 * results of decoration.
 */

public class CombinedLabel {

	private String text;
	private Image image;

	/**
	 * Create a new Decoration beginning with a startText
	 * and a startImage
	 */

	public CombinedLabel(String startText, Image startImage) {
		text = startText;
		image = startImage;
	}

	/**
	 * Return the current text.
	 * @return String
	 */
	public String getText() {
		return text;
	}

	/**
	 * Return the current image.
	 * @return Image
	 */
	public Image getImage() {
		return image;
	}

	/**
	 * Set the current text.
	 * @param String
	 */
	public void setText(String newText) {
		text = newText;
	}

	/**
	 * Set the current image.
	 * @param Image
	 */
	public void setImage(Image newImage) {
		image = newImage;
	}
}