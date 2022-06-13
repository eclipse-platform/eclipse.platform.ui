/*******************************************************************************
 * Copyright (c) 2008, 20156Angelo Zerr and others.
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
 *     Stefan Weiser <stefanfranz.weiser@gmail.com> - Bug 459983
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.resources;

import org.eclipse.e4.ui.css.core.css2.CSS2ColorHelper;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.RGBColor;

/**
 * CSS Resources Helper to manage {@link IResourcesRegistry}.
 *
 */
public class CSSResourcesHelpers {

	public static String getCSSValueKey(CSSValue value) {
		if (value instanceof CSS2FontProperties) {
			return getCSSFontPropertiesKey((CSS2FontProperties) value);
		}
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			return getCSSPrimitiveValueKey((CSSPrimitiveValue) value);
		}
		return null;
	}

	/**
	 * Return the key of the CSSPrimitiveValue <code>value</code> which is
	 * used to cache Resource into {@link IResourcesRegistry}.
	 *
	 * @param value
	 * @return
	 */
	public static String getCSSPrimitiveValueKey(CSSPrimitiveValue value) {
		switch (value.getPrimitiveType()) {
		case CSSPrimitiveValue.CSS_IDENT:
		case CSSPrimitiveValue.CSS_URI:
			String s = value.getStringValue();
			// Test if s is Color Name
			if (CSS2ColorHelper.isColorName(s)) {
				RGBColor rgbColor = CSS2ColorHelper.getRGBColor(s);
				if (rgbColor != null) {
					return getCSSRGBColorKey(rgbColor);
				}
			}
			return value.getStringValue();
		case CSSPrimitiveValue.CSS_RGBCOLOR:
			RGBColor rgbColor = value.getRGBColorValue();
			return getCSSRGBColorKey(rgbColor);
		case CSSPrimitiveValue.CSS_STRING:
			return value.getCssText();
		}
		return null;
	}

	public static String getCSSRGBColorKey(RGBColor rgbColor) {
		if (rgbColor == null) {
			return null;
		}
		StringBuilder rgb = new StringBuilder().append((int) rgbColor.getGreen().getFloatValue(CSSPrimitiveValue.CSS_NUMBER)).append("_");
		rgb.append((int) rgbColor.getRed().getFloatValue(CSSPrimitiveValue.CSS_NUMBER)).append("_");
		rgb.append((int) rgbColor.getBlue().getFloatValue(CSSPrimitiveValue.CSS_NUMBER)).append("");
		return rgb.toString();
	}

	public static String getCSSFontPropertiesKey(CSS2FontProperties fontProperties) {
		return getCssText(fontProperties.getFamily()) + "_" + getCssText(fontProperties.getSize()) + "_"
				+ getCssText(fontProperties.getStyle()) + "_" + getCssText(fontProperties.getWeight());
	}

	private static String getCssText(CSSPrimitiveValue cssPrimitiveValue) {
		if (cssPrimitiveValue != null) {
			return cssPrimitiveValue.getCssText();
		}
		return String.valueOf(cssPrimitiveValue);
	}

	/**
	 * Return the resource type of <code>type</code> cached into
	 * <code>resourcesRegistry</code> with CSSPrimitiveValue
	 * <code>value</code> key.
	 *
	 * @param resourcesRegistry
	 * @param type
	 * @param value
	 * @return
	 */
	public static Object getResource(IResourcesRegistry resourcesRegistry, Object type, CSSPrimitiveValue value) {
		String key = getCSSPrimitiveValueKey(value);
		return getResource(resourcesRegistry, type, key);
	}

	/**
	 * Return the resource type of <code>type</code> cached into
	 * <code>resourcesRegistry</code> with key <code>key</code>.
	 *
	 * @param resourcesRegistry
	 * @param type
	 * @param key
	 * @return
	 */
	public static Object getResource(IResourcesRegistry resourcesRegistry, Object type, String key) {
		if (key == null) {
			return null;
		}
		if (resourcesRegistry != null) {
			return resourcesRegistry.getResource(type, key);
		}
		return null;
	}

	/**
	 * Register the <code>resource</code> type of <code>type</code> into
	 * <code>resourcesRegistry</code> with CSSPrimitiveValue
	 * <code>value</code> key.
	 *
	 * @param resourcesRegistry
	 * @param type
	 * @param value
	 * @param resource
	 */
	public static void registerResource(IResourcesRegistry resourcesRegistry, Object type, CSSPrimitiveValue value,
			Object resource) {
		if (resourcesRegistry != null) {
			String key = getCSSPrimitiveValueKey(value);
			if (key != null) {
				resourcesRegistry.registerResource(type, key, resource);
			}
		}
	}

	/**
	 * Register the <code>resource</code> type of <code>type</code> into
	 * <code>resourcesRegistry</code> with <code>key</code>.
	 *
	 * @param resourcesRegistry
	 * @param type
	 * @param key
	 * @param resource
	 */
	public static void registerResource(IResourcesRegistry resourcesRegistry, Object type, String key,
			Object resource) {
		if (key == null) {
			return;
		}
		if (resourcesRegistry != null) {
			resourcesRegistry.registerResource(type, key, resource);
		}
	}
}
