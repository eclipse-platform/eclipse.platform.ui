package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Image;

/**
 * The Decoration is a class that hodls onto the text and
 * image resulting from a decoration.
 */

public class Decoration {

	private String text;
	private Image image;

	/**
	 * Create a new Decoration beginning with a startText
	 * and a startImage
	 */

	public Decoration(String startText, Image startImage) {
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
	 * @return String
	 */
	public void setText(String newText) {
		text = newText;
	}

	/**
	 * Set the current image.
	 * @return Image
	 */
	public void setImage(Image newImage) {
		image = newImage;
	}
}