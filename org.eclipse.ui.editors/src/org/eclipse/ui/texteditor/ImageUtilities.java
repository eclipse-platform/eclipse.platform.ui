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
package org.eclipse.ui.texteditor;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;


/**
 * @since 3.0
 * @deprecated As of 3.0, replaced by {@link org.eclipse.jface.text.source.ImageUtilities}
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
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.jface.text.source.ImageUtilities#drawImage(Image, GC, Canvas, Rectangle, int, int)}
	 */
	public static void drawImage(Image image, GC gc, Canvas canvas, Rectangle r, int halign, int valign) {
		org.eclipse.jface.text.source.ImageUtilities.drawImage(image, gc, canvas, r, halign, valign);
	}

	/**
	 * Draws an image aligned inside the given rectangle on the given canvas.
	 *
	 * @param image the image to be drawn
	 * @param gc the drawing GC
	 * @param canvas the canvas on which to draw
	 * @param r the clipping rectangle
	 * @param align the alignment of the image to be drawn
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.jface.text.source.ImageUtilities#drawImage(Image, GC, Canvas, Rectangle, int)}
	 */
	public static void drawImage(Image image, GC gc, Canvas canvas, Rectangle r, int align) {
		org.eclipse.jface.text.source.ImageUtilities.drawImage(image, gc, canvas, r, align);
	}
}
