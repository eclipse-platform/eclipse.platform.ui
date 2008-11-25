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


import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationPresentation;


/**
 * Specialized annotation to indicate a particular range of text lines.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * This class is instantiated automatically by <code>AbstractTextEditor</code>.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DefaultRangeIndicator extends Annotation implements IAnnotationPresentation {

	 /** The color palette data of this range indicator */
	private static PaletteData fgPaletteData;
	/** The image of this range indicator */
	private Image fImage;

	/**
	 * Creates a new range indicator.
	 */
	public DefaultRangeIndicator() {
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationPresentation#paint(org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
	 */
	public void paint(GC gc, Canvas canvas, Rectangle bounds) {

		Point canvasSize= canvas.getSize();

		int x= 0;
		int y= bounds.y;
		int w= canvasSize.x;
		int h= bounds.height;
		int b= 1;

		if (y + h > canvasSize.y)
			h= canvasSize.y - y;

		if (y < 0) {
			h= h + y;
			y= 0;
		}

		if (h <= 0)
			return;

		Image image = getImage(canvas);
		gc.drawImage(image, 0, 0, w, h, x, y, w, h);

		gc.setBackground(canvas.getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
		gc.fillRectangle(x, bounds.y, w, b);
		gc.fillRectangle(x, bounds.y + bounds.height - b, w, b);
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
	 */
	public int getLayer() {
		return IAnnotationPresentation.DEFAULT_LAYER;
	}

	/**
	 * Returns the image of this range indicator.
	 *
	 * @param control the control
	 * @return an image
	 */
	private Image getImage(Control control) {
		if (fImage == null) {
				fImage= createImage(control.getDisplay(), control.getSize());

				control.addDisposeListener(new DisposeListener() {
					public void widgetDisposed(DisposeEvent e) {
						if (fImage != null && !fImage.isDisposed()) {
							fImage.dispose();
							fImage= null;
						}
					}
				});
		} else {
			Rectangle imageRectangle= fImage.getBounds();
			Point controlSize= control.getSize();

			if (imageRectangle.width < controlSize.x || imageRectangle.height < controlSize.y) {
				fImage.dispose();
				fImage= createImage(control.getDisplay(), controlSize);
			}
		}

		return fImage;
	}

	/**
	 * Creates and returns a new SWT image with the given size on
	 * the given display which is used as this range indicator's image.
	 *
	 * @param display the display on which to create the image
	 * @param size the image size
	 * @return a new image
 	 */
	private static Image createImage(Display display, Point size) {

		int width= size.x;
		int height= size.y;

		if (fgPaletteData == null)
			fgPaletteData= createPalette(display);

		ImageData imageData= new ImageData(width, height, 1, fgPaletteData);

		for (int y= 0; y < height; y++)
			for (int x= 0; x < width; x++)
				imageData.setPixel(x, y, (x + y) % 2);

		return new Image(display, imageData);
	}

	/**
	 * Creates and returns a new color palette data.
	 *
	 * @param display the display
	 * @return the new color palette data
	 */
	private static PaletteData createPalette(Display display) {
		Color c1;
		Color c2;

		c1= display.getSystemColor(SWT.COLOR_LIST_SELECTION);
		c2= display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

		RGB rgbs[]= new RGB[] {
			new RGB(c1.getRed(), c1.getGreen(), c1.getBlue()),
			new RGB(c2.getRed(), c2.getGreen(), c2.getBlue())};

		return new PaletteData(rgbs);
	}
}
