/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
/**
 * This class extends hyperlink widget by adding the capability to render an
 * image relative to the text. If no text has been set, only image will be
 * shown. Images for hover and active states can be set in addition to the
 * normal state image.
 * <p>
 * When image is taller than the text, additional style can be provided to
 * control vertical alignment (supported values are SWT.TOP, SWT.BOTTOM and
 * SWT.CENTER).
 * <p>The class does not need to be sublassed but it is allowed
 * to do so if some aspect of the image hyperlink needs to be
 * modified.
 * @since 3.0
 */
public class ImageHyperlink extends Hyperlink {
	/**
	 * Amount of pixels between the image and the text (default is 5).
	 */
	public int textSpacing = 5;
	private Image image;
	private Image hoverImage;
	private Image activeImage;
	private int state;
	private static final int HOVER = 1 << 1;
	private static final int ACTIVE = 1 << 2;
	private int verticalAlignment=SWT.CENTER;
	/**
	 * Creates the image hyperlink instance.
	 * 
	 * @param parent
	 *            the control parent
	 * @param style
	 *            the control style (SWT.WRAP, BOTTOM, TOP, MIDDLE)
	 */
	public ImageHyperlink(Composite parent, int style) {
		super(parent, removeAlignment(style));
		extractAlignment(style);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.widgets.AbstractHyperlink#paintHyperlink(org.eclipse.swt.events.PaintEvent)
	 */
	protected void paintHyperlink(GC gc) {
		Rectangle clientArea = getClientArea();
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
			if (verticalAlignment==SWT.BOTTOM) {
				textY = marginHeight + slotHeight - textHeight;
			} else if (verticalAlignment==SWT.CENTER) {
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
	protected void handleEnter(Event e) {
		state = HOVER;
		super.handleEnter(e);
	}
	protected void handleExit(Event e) {
		state = 0;
		super.handleExit(e);
	}
	protected void handleActivate(Event e) {
		state &= ACTIVE;
		redraw();
		super.handleActivate(e);
		state &= ~ACTIVE;
		if (!isDisposed()) 
			redraw();
	}
	/**
	 * Returns active image.
	 * 
	 * @return active image or <code>null</code> if not set.
	 */
	public Image getActiveImage() {
		return activeImage;
	}
	/**
	 * Sets the image to show when link is activated.
	 * 
	 * @param activeImage
	 *  
	 */
	public void setActiveImage(Image activeImage) {
		this.activeImage = activeImage;
	}
	/**
	 * Returns the hover image.
	 * 
	 * @return hover image or <code>null</code> if not set.
	 */
	public Image getHoverImage() {
		return hoverImage;
	}
	/**
	 * Sets the image to show when link is hover state (on mouse over).
	 * 
	 * @param hoverImage
	 */
	public void setHoverImage(Image hoverImage) {
		this.hoverImage = hoverImage;
	}
	/**
	 * Returns the image to show in the normal state.
	 * 
	 * @return normal image or <code>null</code> if not set.
	 */
	public Image getImage() {
		return image;
	}
	/**
	 * Sets the image to show when link is in the normal state.
	 * 
	 * @param image
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
	private static int removeAlignment(int style) {
		if ((style & SWT.CENTER)!=0) {
			return style & (~SWT.CENTER);
		}
		if ((style & SWT.TOP)!=0) {
			return style & (~SWT.TOP);
		}
		if ((style & SWT.BOTTOM)!=0) {
			return style & (~SWT.BOTTOM);
		}
		return style;
	}
	private void extractAlignment(int style) {
		if ((style & SWT.CENTER)!=0) {
			verticalAlignment = SWT.CENTER;
		}
		else if ((style & SWT.TOP)!=0) {
			verticalAlignment = SWT.TOP;
		}
		else if ((style & SWT.BOTTOM)!=0) {
			verticalAlignment = SWT.BOTTOM;
		}
	}
}