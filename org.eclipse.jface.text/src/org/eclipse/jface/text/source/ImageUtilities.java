/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

/**
 * Provides methods for drawing images onto a canvas.
 * <p>
 * This class is neither intended to be instantiated nor subclassed.
 * </p>
 *
 * @since 3.0
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ImageUtilities {

	/**
	 * Draws an image aligned inside the given rectangle on the given canvas.
	 *
	 * @param image the image to be drawn
	 * @param gc the drawing GC
	 * @param canvas the canvas on which to draw
	 * @param r the clipping rectangle
	 * @param halign the horizontal alignment of the image to be drawn
	 * @param valign the vertical alignment of the image to be drawn
	 */
	public static void drawImage(Image image, GC gc, Canvas canvas, Rectangle r, int halign, int valign) {
		if (image != null) {

			Rectangle bounds= image.getBounds();

			int x= 0;
			switch(halign) {
			case SWT.LEFT:
				break;
			case SWT.CENTER:
				x= (r.width - bounds.width) / 2;
				break;
			case SWT.RIGHT:
				x= r.width - bounds.width;
				break;
			}

			int y= 0;
			switch (valign) {
			case SWT.TOP: {
				FontMetrics fontMetrics= gc.getFontMetrics();
				y= (fontMetrics.getHeight() - bounds.height)/2;
				break;
			}
			case SWT.CENTER:
				y= (r.height - bounds.height) / 2;
				break;
			case SWT.BOTTOM: {
				FontMetrics fontMetrics= gc.getFontMetrics();
				y= r.height - (fontMetrics.getHeight() + bounds.height)/2;
				break;
			}
			}

			gc.drawImage(image, r.x+x, r.y+y);
		}
	}

	/**
	 * Draws an image aligned inside the given rectangle on the given canvas.
	 *
	 * @param image the image to be drawn
	 * @param gc the drawing GC
	 * @param canvas the canvas on which to draw
	 * @param r the clipping rectangle
	 * @param align the alignment of the image to be drawn
	 */
	public static void drawImage(Image image, GC gc, Canvas canvas, Rectangle r, int align) {
		drawImage(image, gc, canvas, r, align, SWT.CENTER);
	}
}
