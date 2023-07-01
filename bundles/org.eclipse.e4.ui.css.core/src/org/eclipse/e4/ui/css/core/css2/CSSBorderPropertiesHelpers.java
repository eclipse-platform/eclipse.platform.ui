/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.css2;

import org.eclipse.e4.ui.css.core.dom.properties.CSSBorderProperties;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

/**
 * CSS2 Border Helper.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
public class CSSBorderPropertiesHelpers {

	/**
	 * Update the <code>property of</code> <code>borderProperties</code> instance
	 * with the <code>value</code>.
	 */
	public static void updateCSSProperty(CSSBorderProperties borderProperties, String property, CSSValue value) {
		switch (property) {
		case "border-style":
			updateCSSPropertyBorderStyle(borderProperties, value);
			break;
		case "border-color":
			updateCSSPropertyBorderColor(borderProperties, value);
			break;
		case "border-width":
			updateCSSPropertyBorderWidth(borderProperties, value);
			break;
		default:
			break;
		}
	}

	/**
	 * Update <code>borderProperties</code> instance with border-style
	 * <code>value</code>.
	 *
	 * @param borderProperties
	 * @param value
	 */
	public static void updateCSSPropertyBorderStyle(CSSBorderProperties borderProperties, CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			borderProperties.setStyle(((CSSPrimitiveValue) value).getStringValue());
		}
	}

	/**
	 * Update <code>borderProperties</code> instance with border-color
	 * <code>value</code>.
	 *
	 * @param borderProperties
	 * @param value
	 */
	public static void updateCSSPropertyBorderColor(CSSBorderProperties borderProperties, CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			borderProperties.setColor((CSSPrimitiveValue) value);
		}
	}

	/**
	 * Update <code>borderProperties</code> instance with border-width
	 * <code>value</code>.
	 *
	 * @param borderProperties
	 * @param value
	 */
	public static void updateCSSPropertyBorderWidth(CSSBorderProperties borderProperties, CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			borderProperties.setWidth((int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PT));
		}
	}

}
