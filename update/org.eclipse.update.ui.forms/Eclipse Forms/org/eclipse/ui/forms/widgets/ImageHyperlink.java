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
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ImageHyperlink extends Hyperlink {
	private Image image;
	private Image hoverImage;
	private Image activeImage;
	private int state;
	private static final int HOVER = 1 << 1;
	private static final int ACTIVE = 1 << 2;
	
	public int textSpacing = 5;
	/**
	 * @param parent
	 * @param style
	 */
	public ImageHyperlink(Composite parent, int style) {
		super(parent, style);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.widgets.AbstractHyperlink#paintHyperlink(org.eclipse.swt.events.PaintEvent)
	 */
	protected void paintHyperlink(PaintEvent e) {
		Rectangle clientArea = getClientArea();
		GC gc = e.gc;
		Image image = null;
		if ((state & ACTIVE)!=0)
			image = activeImage;
		else if ((state & HOVER)!=0)
			image = hoverImage;
		if (image==null) image = this.image;
		if (image==null) return;
		Rectangle ibounds = image.getBounds();
		Point maxsize = computeImageSize();
		int x = marginWidth + maxsize.x/2-ibounds.width/2;
		int y = marginHeight + maxsize.y/2-ibounds.height/2;
		gc.drawImage(image, x, y);
		if (getText()!=null) {
			Rectangle tbounds = new Rectangle(marginWidth+maxsize.x+textSpacing,
					marginHeight,
					clientArea.width-maxsize.x-marginWidth-marginWidth,
					clientArea.height-marginHeight-marginHeight);
			paintText(gc, tbounds);
		}
	}
	
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();

		Point isize = computeImageSize();
		Point textSize = null;
		if (getText()!=null) {
			int innerWHint = wHint;
			if (wHint!=SWT.DEFAULT) {
				innerWHint = wHint - 2*marginWidth;
			}
			textSize = super.computeSize(innerWHint, hHint, changed);
		}
		int width = isize.x;
		int height = isize.y;
		if (textSize!=null) {
			width += textSpacing;
			width += textSize.x;
			height = Math.max(height, textSize.y);
		}
		width += 2 * marginWidth;
		height += 2 * marginHeight;
		return new Point(width, height);
	}	
	
	private Point computeImageSize() {
		int x = 0;
		int y = 0;
		if (image!=null) {
			x = Math.max(image.getBounds().width, x);
			y = Math.max(image.getBounds().height, y);
		}
		if (hoverImage!=null) {
			x = Math.max(hoverImage.getBounds().width, x);
			y = Math.max(hoverImage.getBounds().height, y);
		}
		if (activeImage!=null) {
			x = Math.max(activeImage.getBounds().width, x);
			y = Math.max(activeImage.getBounds().height, y);
		}
		return new Point(x, y);
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
	 * @param activeImage The activeImage to set.
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
	 * @param hoverImage The hoverImage to set.
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
	 * @param image The image to set.
	 */
	public void setImage(Image image) {
		this.image = image;
	}

}
