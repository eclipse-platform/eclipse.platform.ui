/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	 * Update the <code>property of</code> <code>borderProperties</code>
	 * instance with the <code>value</code>.
	 * 
	 * @param border
	 * @param property
	 * @param value
	 */
	public static void updateCSSProperty(CSSBorderProperties borderProperties,
			String property, CSSValue value) {
		if ("border-style".equals(property))
			updateCSSPropertyBorderStyle(borderProperties, value);
		else if ("border-color".equals(property))
			updateCSSPropertyBorderColor(borderProperties, value);
		else if ("border-width".equals(property))
			updateCSSPropertyBorderWidth(borderProperties, value);
	}

	/**
	 * Update <code>borderProperties</code> instance with border-style
	 * <code>value</code>.
	 * 
	 * @param borderProperties
	 * @param value
	 */
	public static void updateCSSPropertyBorderStyle(
			CSSBorderProperties borderProperties, CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			borderProperties.setStyle(((CSSPrimitiveValue) value)
					.getStringValue());
		}
	}

	/**
	 * Update <code>borderProperties</code> instance with border-color
	 * <code>value</code>.
	 * 
	 * @param borderProperties
	 * @param value
	 */
	public static void updateCSSPropertyBorderColor(
			CSSBorderProperties borderProperties, CSSValue value) {
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
	public static void updateCSSPropertyBorderWidth(
			CSSBorderProperties borderProperties, CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			borderProperties.setWidth((int) ((CSSPrimitiveValue) value)
					.getFloatValue(CSSPrimitiveValue.CSS_PT));
		}
	}

}
