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
package org.eclipse.jface.text.source;



import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

/**
 * Abstract annotation managed by an <code>IAnnotationModel</code>.
 * Annotations are considered being located at layers and are considered being painted
 * starting with layer 0 upwards. Thus an annotation of layer 5 will be drawn on top of
 * all co-located annotations at the layers 4 - 0. Subclasses must provide the annotations
 * paint method.
 *
 * @see IVerticalRuler
 */
public abstract class Annotation {
	
	/** The layer of this annotation. */
	private int fLayer;
	
	/**
	 * Creates a new annotation.
	 */
	protected Annotation() {
	}
	
	/**
	 * Sets the layer of this annotation.
	 *
	 * @param layer the layer of this annotation
	 */
	protected void setLayer(int layer) {
		fLayer= layer;
	}	
	
	/**
	 * Convenience method for drawing an image aligned inside a rectangle.
	 *
	 * @param image the image to be drawn
	 * @param GC the drawing GC
	 * @param canvas the canvas on which to draw
	 * @param r the clipping rectangle
	 * @param halign the horizontal alignment of the image to be drawn
	 * @param valign the vertical alignment of the image to be drawn
	 */
	protected static void drawImage(Image image, GC gc, Canvas canvas, Rectangle r, int halign, int valign) {
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
				case SWT.TOP:
					FontMetrics fontMetrics= gc.getFontMetrics();
					y= fontMetrics.getHeight() + fontMetrics.getAscent() + fontMetrics.getDescent() - image.getBounds().height / 2;
					break;
				case SWT.CENTER:
					y= (r.height - bounds.height) / 2;
					break;
				case SWT.BOTTOM:
					y= r.height - bounds.height;
					break;
			}
			
			gc.drawImage(image, r.x+x, r.y+y);
		}
	}
	
	/**
	 * Convenience method for drawing an image aligned inside a rectangle.
	 *
	 * @param image the image to be drawn
	 * @param GC the drawing GC
	 * @param canvas the canvas on which to draw
	 * @param r the clipping rectangle
	 * @param align the alignment of the image to be drawn
	 */
	protected static void drawImage(Image image, GC gc, Canvas canvas, Rectangle r, int align) {
		drawImage(image, gc, canvas, r, align, SWT.CENTER);
	}
	
	/**
	 * Returns the annotations drawing layer.
	 *
	 * @return the annotations drawing layer
	 */
	public int getLayer() {
		return fLayer;
	}
	
	/**
	 * Implement this method to draw a graphical representation 
	 * of this annotation within the given bounds.
	 *
	 * @param GC the drawing GC
	 * @param canvas the canvas to draw on
	 * @param bounds the bounds inside the canvas to draw on
	 */
	public abstract void paint(GC gc, Canvas canvas, Rectangle bounds);
}
