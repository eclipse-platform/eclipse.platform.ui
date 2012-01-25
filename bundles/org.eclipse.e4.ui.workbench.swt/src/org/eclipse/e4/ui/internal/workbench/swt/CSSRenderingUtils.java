/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import javax.inject.Inject;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.ControlElement;
import org.eclipse.e4.ui.css.swt.internal.theme.ThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class CSSRenderingUtils {
	private ThemeEngine themeEngine;

	@Inject
	void getThemeEngine(IThemeEngine engine) {
		if (engine instanceof ThemeEngine) {
			themeEngine = (ThemeEngine) engine;
		}
	}

	// public Control frameMeIfPossible(Control toFrame, String classId,
	// boolean vertical, boolean draggable) {
	// Integer[] frameInts = new Integer[4];
	// Image frameImage = createImage(toFrame, classId, "frame-image",
	// frameInts);
	// Image handleImage = createImage(toFrame, classId, "handle-image", null);
	// if (frameImage != null) {
	// ImageBasedFrame frame = new ImageBasedFrame(toFrame.getParent(),
	// toFrame, vertical, draggable);
	// frame.setImages(frameImage, frameInts, handleImage);
	// return frame;
	// }
	//
	// return toFrame;
	// }

	@SuppressWarnings("restriction")
	public CSSValue getCSSValue(Control styleControl, String className,
			String attributeName) {
		CSSEngine csseng = themeEngine.getCSSEngine();

		// super hack
		ControlElement tempEment = new ControlElement(styleControl, csseng);
		ControlElement.setCSSClass(styleControl, className);

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

		//		System.out.println("THeme engine " + themeEngine); //$NON-NLS-1$
		if (themeEngine instanceof ThemeEngine) {
			CSSEngine csseng = ((ThemeEngine) themeEngine).getCSSEngine();

			// super hack
			ControlElement tempEment = new ControlElement(styleControl, csseng);
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
				String imageString = ((CSSPrimitiveValue) imagePath)
						.getStringValue();
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
		}
		return image;
	}
}
