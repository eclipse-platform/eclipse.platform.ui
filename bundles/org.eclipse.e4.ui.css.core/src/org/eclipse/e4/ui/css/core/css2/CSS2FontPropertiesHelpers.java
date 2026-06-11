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

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontPropertiesImpl;
import org.eclipse.e4.ui.css.core.engine.CSSElementContext;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssList;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.w3c.dom.css.CSSValue;

/**
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 */
public class CSS2FontPropertiesHelpers {

	/**
	 * Constant used to store {@link CSS2FontProperties} instance into
	 * {@link CSSElementContext} context.
	 */
	public static final String CSS2FONT_KEY = "org.eclipse.e4.ui.css.core.css2.CSS2FONT_KEY";

	/**
	 * Get {@link CSS2FontProperties} from {@link CSSElementContext} context.
	 */
	public static CSS2FontProperties getCSS2FontProperties(CSSElementContext context) {
		// Search into Data of context if CSS2FontProperties exist.
		return (CSS2FontProperties) context.getData(CSS2FONT_KEY);
	}

	/**
	 * Set {@link CSS2FontProperties} <code>fontProperties</code> into
	 * {@link CSSElementContext} context.
	 */
	public static void setCSS2FontProperties(CSS2FontProperties fontProperties, CSSElementContext context) {
		if (fontProperties == null) {
			return;
		}
		context.setData(CSS2FONT_KEY, fontProperties);
	}

	/**
	 * Create and return {@link CSS2FontProperties} instance from
	 * <code>value</code>.
	 */
	public static CSS2FontProperties createCSS2FontProperties(CSSValue value, String property) {
		CSS2FontProperties fontProperties = new CSS2FontPropertiesImpl();
		updateCSSPropertyFont(fontProperties, property, value);
		return fontProperties;
	}

	/**
	 * Update the <code>property of</code> <code>fontProperties</code> instance with
	 * the <code>value</code>.
	 */
	public static void updateCSSPropertyFont(CSS2FontProperties fontProperties, String property, CSSValue value) {
		switch (property) {
		case "font-family":
			updateCSSPropertyFontFamily(fontProperties, value);
			break;
		case "font-size":
			updateCSSPropertyFontSize(fontProperties, value);
			break;
		case "font-style":
			updateCSSPropertyFontStyle(fontProperties, value);
			break;
		case "font-weight":
			updateCSSPropertyFontWeight(fontProperties, value);
			break;
		case "font":
			updateCSSPropertyFontComposite(fontProperties, value);
			break;
		default:
			break;
		}
	}

	/**
	 * Update <code>fontProperties</code> instance with the {@link CSSValue}
	 * <code>value</code>. value can be a single value or a value list.
	 */
	public static void updateCSSPropertyFontComposite(CSS2FontProperties font, CSSValue value) {
		if (value instanceof CssList list) {
			for (CSSValue item : list.values()) {
				updateCSSPropertyFontComposite(font, item);
			}
		} else if (value instanceof CssPrimitive primitive) {
			String property = CSS2FontHelper.getCSSFontPropertyName(primitive);
			updateCSSPropertyFont(font, property, value);
		}
	}

	/**
	 * Update CSS2FontProperties instance with font-family.
	 */
	public static void updateCSSPropertyFontFamily(CSS2FontProperties font, CSSValue value) {
		if (value instanceof CssPrimitive primitive) {
			font.setFamily(primitive);
		}
	}

	/**
	 * Update CSS2FontProperties instance with font-size.
	 */
	public static void updateCSSPropertyFontSize(CSS2FontProperties font, CSSValue value) {
		if (value instanceof CssPrimitive primitive) {
			font.setSize(primitive);
			font.setSizeFromCSS(true);
		}
	}

	/**
	 * Update CSS2FontProperties instance with font-style.
	 */
	public static void updateCSSPropertyFontStyle(CSS2FontProperties font, CSSValue value) {
		if (value instanceof CssPrimitive primitive) {
			font.setStyle(primitive);
		}
	}

	/**
	 * Update CSS2FontProperties instance with font-weight.
	 */
	public static void updateCSSPropertyFontWeight(CSS2FontProperties font, CSSValue value) {
		if (value instanceof CssPrimitive primitive) {
			font.setWeight(primitive);
		}
	}
}
