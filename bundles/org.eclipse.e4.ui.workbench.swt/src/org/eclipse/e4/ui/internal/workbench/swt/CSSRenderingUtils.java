/*******************************************************************************
 * Copyright (c) 2012, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.ControlElement;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.widgets.ImageBasedFrame;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class CSSRenderingUtils {

	// NOTE: The CSS engine 'owns' the image it returns (it caches it)
	// so we have to cache any rotated versions to match
	private Map<Image, Image> rotatedImageMap = new HashMap<Image, Image>();

	public Control frameMeIfPossible(Control toFrame, String classId,
			boolean vertical, boolean draggable) {
		Integer[] frameInts = new Integer[4];
		Image frameImage = createImage(toFrame, classId, "frame-image",
				frameInts);
		if (vertical && frameImage != null)
			frameImage = rotateImage(toFrame.getDisplay(), frameImage,
					frameInts);

		Image handleImage = createImage(toFrame, classId, "handle-image", null);
		if (vertical && handleImage != null)
			handleImage = rotateImage(toFrame.getDisplay(), handleImage, null);

		if (frameImage != null) {
			ImageBasedFrame frame = new ImageBasedFrame(toFrame.getParent(),
					toFrame, vertical, draggable);
			frame.setImages(frameImage, frameInts, handleImage);
			return frame;
		} else if (handleImage != null) {
			ImageBasedFrame frame = new ImageBasedFrame(toFrame.getParent(),
					toFrame, vertical, draggable);
			frame.setImages(null, null, handleImage);
			return frame;
		}

		return toFrame;
	}

	private Image rotateImage(Display display, Image image, Integer[] frameInts) {
		// Swap the widths / heights of the 'cuts'
		if (frameInts != null) {
			int tmp;
			tmp = frameInts[0];
			frameInts[0] = frameInts[2];
			frameInts[2] = tmp;
			tmp = frameInts[1];
			frameInts[1] = frameInts[3];
			frameInts[3] = tmp;
		}

		if (rotatedImageMap.get(image) != null)
			return rotatedImageMap.get(image);

		// rotate 90 degrees,,,
		Rectangle bounds = image.getBounds();
		ImageData imageData = new ImageData(bounds.height, bounds.width, 32,
				new PaletteData(0xFF0000, 0x00FF00, 0x0000FF));
		Image rotatedImage = new Image(display, imageData);
		GC gc = new GC(rotatedImage);
		RGB rgb = new RGB(0x7d, 0, 0);
		Color offRed = new Color(display, rgb);
		gc.setBackground(offRed);
		gc.fillRectangle(0, 0, bounds.height, bounds.width);
		Transform t = new Transform(display);
		int w = image.getBounds().height;
		int offset = 0; // (w+1) % 2;
		t.translate(w - offset, 0);
		t.rotate(90);
		gc.setTransform(t);
		gc.drawImage(image, 0, 0);
		gc.dispose();
		t.dispose();
		offRed.dispose();
		ImageData alphaData = rotatedImage.getImageData();
		rotatedImage.dispose();
		int transparentPix = alphaData.palette.getPixel(rgb);
		for (int i = 0; i < alphaData.width; i++) {
			for (int j = 0; j < alphaData.height; j++) {
				if (alphaData.getPixel(i, j) != transparentPix) {
					alphaData.setAlpha(i, j, 255);
				}
			}
		}
		rotatedImage = new Image(display, alphaData);
		// ...and cache it
		rotatedImageMap.put(image, rotatedImage);

		// Return the new one
		return rotatedImage;
	}

	public CSSValue getCSSValue(Control styleControl, String className,
			String attributeName) {
		CSSEngine csseng = WidgetElement.getEngine(styleControl);
		if (csseng == null) {
			return null;
		}
		ControlElement tempEment = (ControlElement) csseng
				.getElement(styleControl);
		if (tempEment == null) {
			return null;
		}

		// super hack
		if (className != null)
			WidgetElement.setCSSClass(styleControl, className);

		CSSStyleDeclaration styleDeclarations = csseng.getViewCSS()
				.getComputedStyle(tempEment, ""); //$NON-NLS-1$

		if (styleDeclarations == null)
			return null;

		return styleDeclarations.getPropertyCSSValue(attributeName);
	}

	/**
	 * @param string
	 * @param string2
	 * @return
	 */
	public Image createImage(Control styleControl, String classId,
			String attName, Integer[] frameInts) {
		Image image = null;

		CSSEngine csseng = WidgetElement.getEngine(styleControl);
		if (csseng == null) {
			return null;
		}
		ControlElement tempEment = (ControlElement) csseng
				.getElement(styleControl);
		if (tempEment == null) {
			return null;
		}
		if (classId != null)
			ControlElement.setCSSClass(styleControl, classId); //$NON-NLS-1$

		CSSStyleDeclaration styleDeclarations = csseng.getViewCSS()
				.getComputedStyle(tempEment, "");
		if (styleDeclarations == null)
			return null;

		CSSValue imagePath = styleDeclarations.getPropertyCSSValue(attName); //$NON-NLS-1$
		if (imagePath == null)
			return null;

		if (imagePath != null
				&& imagePath.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			// String imageString = ((CSSPrimitiveValue) imagePath)
			// .getStringValue();
			// System.out.println("here" + imageString);
			try {
				image = (Image) csseng.convert(imagePath, Image.class,
						styleControl.getDisplay());
				if (image != null && frameInts != null) {
					CSSValue value = styleDeclarations
							.getPropertyCSSValue("frame-cuts"); //$NON-NLS-1$
					if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
						CSSValueList valueList = (CSSValueList) value;
						if (valueList.getLength() != 4)
							return null;

						for (int i = 0; i < valueList.getLength(); i++) {
							CSSValue val = valueList.item(i);
							if ((val.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)
									&& ((CSSPrimitiveValue) val)
											.getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
								frameInts[i] = (int) ((CSSPrimitiveValue) val)
										.getFloatValue(CSSPrimitiveValue.CSS_PX);
							} else {
								return null;
							}
						}

						// System.out.println("Results " + frameInts);
					}
				}
			} catch (Exception e1) {
			}
		}
		return image;
	}
}
