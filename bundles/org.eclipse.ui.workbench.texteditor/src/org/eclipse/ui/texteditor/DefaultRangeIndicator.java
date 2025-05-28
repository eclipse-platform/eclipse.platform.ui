/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.texteditor;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.JFaceResources;

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

	private static final String RANGE_INDICATOR_COLOR= "org.eclipse.ui.editors.rangeIndicatorColor"; //$NON-NLS-1$
	/** The image of this range indicator */
	private Image fImage;
	/** The color used to draw the range indicator during the last paint action. */
	private Color fLastRangeIndicatorColor;

	/**
	 * Creates a new range indicator.
	 */
	public DefaultRangeIndicator() {
	}

	@Override
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

		Color currentRangeIndicatorColor= JFaceResources.getColorRegistry().get(RANGE_INDICATOR_COLOR);
		Image image= getImage(canvas, currentRangeIndicatorColor);
		gc.drawImage(image, 0, 0, w, h, x, y, w, h);

		gc.setBackground(currentRangeIndicatorColor);
		gc.fillRectangle(x, bounds.y, w, b);
		gc.fillRectangle(x, bounds.y + bounds.height - b, w, b);

		fLastRangeIndicatorColor= currentRangeIndicatorColor;
	}

	@Override
	public int getLayer() {
		return IAnnotationPresentation.DEFAULT_LAYER;
	}

	/**
	 * Returns the image of this range indicator.
	 *
	 * @param control the control
	 * @param rangeIndicatorColor the color to be used to paint the range indicator
	 * @return an image
	 */
	private Image getImage(Control control, Color rangeIndicatorColor) {
		if (fImage == null) {
			fImage= createImage(control.getDisplay(), control.getSize(), rangeIndicatorColor);

			control.addDisposeListener(e -> {
				if (fImage != null && !fImage.isDisposed()) {
					fImage.dispose();
					fImage = null;
				}
			});
		} else {
			Rectangle imageRectangle= fImage.getBounds();
			Point controlSize= control.getSize();

			if (imageRectangle.width < controlSize.x || imageRectangle.height < controlSize.y
					|| !rangeIndicatorColor.equals(fLastRangeIndicatorColor)) {
				fImage.dispose();
				fImage= createImage(control.getDisplay(), controlSize, rangeIndicatorColor);
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
	 * @param rangeIndicatorColor the color to be used to paint the range indicator
	 * @return a new image
	 */
	private static Image createImage(Display display, Point size, Color rangeIndicatorColor) {

		int width = size.x;
		int height = size.y;

		ImageDataProvider imageDataProvider = zoom -> {
			float scaleFactor = zoom / 100.0f;
			int scaledWidth = Math.round(width * scaleFactor);
			int scaledHeight = Math.round(height * scaleFactor);
			ImageData imageData = new ImageData(scaledWidth, scaledHeight, 1,
					createPalette(display, rangeIndicatorColor));
			int blockSize = Math.round(scaleFactor);
			for (int y = 0; y < scaledHeight; y++) {
				for (int x = 0; x < scaledWidth; x++) {
					if (((x / blockSize) + (y / blockSize)) % 2 == 0) {
						imageData.setPixel(x, y, 1);
					}
				}
			}
			imageData.transparentPixel = 1;
			return imageData;
		};

		return new Image(display, imageDataProvider);
	}

	/**
	 * Creates and returns a new color palette data.
	 *
	 * @param display the display
	 * @param rangeIndicatorColor the color to be used to paint the range indicator
	 * @return the new color palette data
	 */
	private static PaletteData createPalette(Display display, Color rangeIndicatorColor) {
		return new PaletteData(new RGB[] { rangeIndicatorColor.getRGB(),
				display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB() });
	}
}
