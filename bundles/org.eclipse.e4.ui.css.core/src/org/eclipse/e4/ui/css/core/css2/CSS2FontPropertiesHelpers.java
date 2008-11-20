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

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontPropertiesImpl;
import org.eclipse.e4.ui.css.core.engine.CSSElementContext;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

/**
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class CSS2FontPropertiesHelpers {

	/**
	 * Constant used to store {@link CSS2FontProperties} instance into
	 * {@link CSSElementContext} context.
	 */
	public static final String CSS2FONT_KEY = "org.eclipse.e4.ui.css.core.css2.CSS2FONT_KEY";

	/**
	 * Get {@link CSS2FontProperties} from {@link CSSElementContext} context.
	 * 
	 * @param control
	 * @return
	 */
	public static CSS2FontProperties getCSS2FontProperties(
			CSSElementContext context) {
		// Search into Data of context if CSS2FontProperties exist.
		return (CSS2FontProperties) context.getData(CSS2FONT_KEY);
	}

	/**
	 * Set {@link CSS2FontProperties} <code>fontProperties</code> into
	 * {@link CSSElementContext} context.
	 * 
	 * @param fontProperties
	 * @param context
	 */
	public static void setCSS2FontProperties(CSS2FontProperties fontProperties,
			CSSElementContext context) {
		if (fontProperties == null)
			return;
		context.setData(CSS2FONT_KEY, fontProperties);
	}

	/**
	 * Create and return {@link CSS2FontProperties} instance from
	 * <code>value</code>.
	 * 
	 * @param value
	 * @param property
	 * @return
	 */
	public static CSS2FontProperties createCSS2FontProperties(CSSValue value,
			String property) {
		CSS2FontProperties fontProperties = new CSS2FontPropertiesImpl();
		updateCSSPropertyFont(fontProperties, property, value);
		return fontProperties;
	}

	/**
	 * Update the <code>property of</code> <code>fontProperties</code>
	 * instance with the <code>value</code>.
	 * 
	 * @param fontProperties
	 * @param property
	 * @param value
	 */
	public static void updateCSSPropertyFont(CSS2FontProperties fontProperties,
			String property, CSSValue value) {
		if ("font-family".equals(property))
			updateCSSPropertyFontFamily(fontProperties, value);
		else if ("font-size".equals(property))
			updateCSSPropertyFontSize(fontProperties, value);
		else if ("font-style".equals(property))
			updateCSSPropertyFontStyle(fontProperties, value);
		else if ("font-weight".equals(property))
			updateCSSPropertyFontWeight(fontProperties, value);
		else if ("font".equals(property))
			updateCSSPropertyFontComposite(fontProperties, value);
	}

	/**
	 * Update <code>fontProperties</code> instance with the {@link CSSValue}
	 * <code>value</code>. value can be {@link CSSPrimitiveValue} or
	 * {@link CSSValueList}.
	 * 
	 * @param font
	 * @param value
	 */
	public static void updateCSSPropertyFontComposite(CSS2FontProperties font,
			CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			CSSValueList valueList = (CSSValueList) value;
			int length = valueList.getLength();
			for (int i = 0; i < length; i++) {
				CSSValue value2 = valueList.item(i);
				updateCSSPropertyFontComposite(font, value2);
			}
		} else {
			if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				String property = CSS2FontHelper
						.getCSSFontPropertyName((CSSPrimitiveValue) value);
				updateCSSPropertyFont(font, property, value);
			}
		}
	}

	/**
	 * Update CSS2FontProperties instance with font-family.
	 * 
	 * @param font
	 * @param value
	 * @throws Exception
	 */
	public static void updateCSSPropertyFontFamily(CSS2FontProperties font,
			CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			font.setFamily((CSSPrimitiveValue) value);
		}
	}

	/**
	 * Update CSS2FontProperties instance with font-size.
	 * 
	 * @param font
	 * @param value
	 * @throws Exception
	 */
	public static void updateCSSPropertyFontSize(CSS2FontProperties font,
			CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			font.setSize((CSSPrimitiveValue) value);
		}
	}

	/**
	 * Update CSS2FontProperties instance with font-style.
	 * 
	 * @param font
	 * @param value
	 * @throws Exception
	 */
	public static void updateCSSPropertyFontStyle(CSS2FontProperties font,
			CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			font.setStyle((CSSPrimitiveValue) value);
		}
	}

	/**
	 * Update CSS2FontProperties instance with font-weight.
	 * 
	 * @param font
	 * @param value
	 * @throws Exception
	 */
	public static void updateCSSPropertyFontWeight(CSS2FontProperties font,
			CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			font.setWeight((CSSPrimitiveValue) value);
		}
	}
}
