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
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssColor;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssText;
import org.w3c.dom.css.CSSValue;

/**
 * CSS Resources Helper to manage {@link IResourcesRegistry}.
 */
public class CSSResourcesHelpers {

	public static String getCSSValueKey(CSSValue value) {
		if (value instanceof CSS2FontProperties) {
			return getCSSFontPropertiesKey((CSS2FontProperties) value);
		}
		if (value instanceof CssPrimitive primitive) {
			return getCSSPrimitiveValueKey(primitive);
		}
		return null;
	}

	/**
	 * Return the key of the CSSPrimitiveValue <code>value</code> which is
	 * used to cache Resource into {@link IResourcesRegistry}.
	 */
	public static String getCSSPrimitiveValueKey(CssPrimitive value) {
		if (value instanceof CssText text) {
			switch (text.kind()) {
			case IDENT:
			case URI:
				String s = text.value();
				// Test if s is Color Name
				if (CSS2ColorHelper.isColorName(s)) {
					CssColor rgbColor = CSS2ColorHelper.getRGBColor(s);
					if (rgbColor != null) {
						return getCSSRGBColorKey(rgbColor);
					}
				}
				return text.value();
			case STRING:
				return text.getCssText();
			default:
				return null;
			}
		}
		if (value instanceof CssColor color) {
			return getCSSRGBColorKey(color);
		}
		return null;
	}

	public static String getCSSRGBColorKey(CssColor rgbColor) {
		if (rgbColor == null) {
			return null;
		}
		StringBuilder rgb = new StringBuilder().append((int) rgbColor.green().value()).append("_");
		rgb.append((int) rgbColor.red().value()).append("_");
		rgb.append((int) rgbColor.blue().value()).append("");
		return rgb.toString();
	}

	public static String getCSSFontPropertiesKey(CSS2FontProperties fontProperties) {
		return getCssText(fontProperties.getFamily()) + "_" + getCssText(fontProperties.getSize()) + "_"
				+ getCssText(fontProperties.getStyle()) + "_" + getCssText(fontProperties.getWeight());
	}

	private static String getCssText(CssPrimitive primitive) {
		if (primitive != null) {
			return primitive.getCssText();
		}
		return String.valueOf(primitive);
	}

	/**
	 * Return the resource type of <code>type</code> cached into
	 * <code>resourcesRegistry</code> with CSSPrimitiveValue
	 * <code>value</code> key.
	 */
	public static Object getResource(IResourcesRegistry resourcesRegistry, Object type, CssPrimitive value) {
		String key = getCSSPrimitiveValueKey(value);
		return getResource(resourcesRegistry, type, key);
	}

	/**
	 * Return the resource type of <code>type</code> cached into
	 * <code>resourcesRegistry</code> with key <code>key</code>.
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
	 */
	public static void registerResource(IResourcesRegistry resourcesRegistry, Object type, CssPrimitive value,
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
