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
package org.eclipse.ui.forms.widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;
/**
 * This class extends hyperlink widget by adding the capability to render an
 * image relative to the text. If no text has been set, only image will be
 * shown. Images for hover and active states can be set in addition to the
 * normal state image.
 */
public class ImageHyperlink extends Hyperlink {
	/**
	 * Aligns text with the top of the image (value is SWT.TOP)
	 */
	public static final int TOP = SWT.TOP;
	/**
	 * Aligns text with the bottom of the image (value is SWT.BOTTOM)
	 */
	public static final int BOTTOM = SWT.BOTTOM;
	/**
	 * Centers text vertically in respect to the image (value is SWT.BOTTOM
	 * &lt;&lt; 1)
	 */
	public static final int MIDDLE = SWT.CENTER;
	private Image image;
	private Image hoverImage;
	private Image activeImage;
	private int state;
	private static final int HOVER = 1 << 1;
	private static final int ACTIVE = 1 << 2;
	/**
	 * Amount of pixels between the image and the text (default is 5).
	 */
	public int textSpacing = 5;
	/**
	 * Creates the image hyperlink instance.
	 * 
	 * @param parent
	 *            the control parent
	 * @param style
	 *            the control style (SWT.WRAP, BOTTOM, TOP, MIDDLE)
	 */
	public ImageHyperlink(Composite parent, int style) {
		super(parent, style);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.widgets.AbstractHyperlink#paintHyperlink(org.eclipse.swt.events.PaintEvent)
	 */
	protected void paintHyperlink(PaintEvent e) {
		Rectangle clientArea = getClientArea();
		GC gc = e.gc;
		Image image = null;
		if ((state & ACTIVE) != 0)
			image = activeImage;
		else if ((state & HOVER) != 0)
			image = hoverImage;
		if (image == null)
			image = this.image;
		if (image == null)
			return;
		Rectangle ibounds = image.getBounds();
		Point maxsize = computeMaxImageSize();
		int x = marginWidth + maxsize.x / 2 - ibounds.width / 2;
		int y = marginHeight + maxsize.y / 2 - ibounds.height / 2;
		gc.drawImage(image, x, y);
		if (getText() != null) {
			int textWidth = clientArea.width - maxsize.x - textSpacing
					- marginWidth - marginWidth;
			int textX = marginWidth + maxsize.x + textSpacing;
			Point textSize = computeTextSize(textWidth, SWT.DEFAULT);
			textWidth = textSize.x;
			int slotHeight = clientArea.height - marginHeight - marginHeight;
			int textY;
			int textHeight = textSize.y;
			if ((getStyle() & BOTTOM) != 0) {
				textY = marginHeight + slotHeight - textHeight;
			} else if ((getStyle() & MIDDLE) != 0) {
				textY = marginHeight + slotHeight / 2 - textHeight / 2;
			} else {
				textY = marginHeight;
			}
			paintText(gc, new Rectangle(textX, textY, textWidth, textHeight));
		}
	}
	/**
	 * Computes the control size by reserving space for images in addition to
	 * text.
	 * 
	 * @param wHint
	 *            width hint
	 * @param hHint
	 *            height hint
	 * @param changed
	 *            if <code>true</code>, any cached layout data should be
	 *            computed anew
	 */
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();
		Point isize = computeMaxImageSize();
		Point textSize = null;
		if (getText() != null) {
			int innerWHint = wHint;
			if (wHint != SWT.DEFAULT) {
				innerWHint = wHint - 2 * marginWidth;
			}
			textSize = super.computeSize(innerWHint, hHint, changed);
		}
		int width = isize.x;
		int height = isize.y;
		if (textSize != null) {
			width += textSpacing;
			width += textSize.x;
			height = Math.max(height, textSize.y);
		}
		width += 2 * marginWidth;
		height += 2 * marginHeight;
		return new Point(width, height);
	}
	protected void handleEnter() {
		state = HOVER;
		super.handleEnter();
	}
	protected void handleExit() {
		state = 0;
		super.handleExit();
	}
	protected void handleActivate() {
		state &= ACTIVE;
		redraw();
		super.handleActivate();
		state &= ~ACTIVE;
		redraw();
	}
	/**
	 * @return Returns the activeImage.
	 */
	public Image getActiveImage() {
		return activeImage;
	}
	/**
	 * @param activeImage
	 *            The activeImage to set.
	 */
	public void setActiveImage(Image activeImage) {
		this.activeImage = activeImage;
	}
	/**
	 * @return Returns the hoverImage.
	 */
	public Image getHoverImage() {
		return hoverImage;
	}
	/**
	 * @param hoverImage
	 *            The hoverImage to set.
	 */
	public void setHoverImage(Image hoverImage) {
		this.hoverImage = hoverImage;
	}
	/**
	 * @return Returns the image.
	 */
	public Image getImage() {
		return image;
	}
	/**
	 * @param image
	 *            The image to set.
	 */
	public void setImage(Image image) {
		this.image = image;
	}
	private Point computeMaxImageSize() {
		int x = 0;
		int y = 0;
		if (image != null) {
			x = Math.max(image.getBounds().width, x);
			y = Math.max(image.getBounds().height, y);
		}
		if (hoverImage != null) {
			x = Math.max(hoverImage.getBounds().width, x);
			y = Math.max(hoverImage.getBounds().height, y);
		}
		if (activeImage != null) {
			x = Math.max(activeImage.getBounds().width, x);
			y = Math.max(activeImage.getBounds().height, y);
		}
		return new Point(x, y);
	}
}