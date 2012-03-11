/*******************************************************************************
 * Copyright (c) 2008, 2009 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties.providers;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyBackgroundHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyBorderHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyClassificationHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyDimensionHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyFontHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyMarginHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyPaddingHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyTextHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * CSS property handler with static strategy. {@link ICSSPropertyHandler} are
 * retrieved after the CSS Engine register the handler with
 * registerCSSPropertyHandler method.
 */
public class CSSPropertyHandlerSimpleProviderImpl extends
		AbstractCSSPropertyHandlerProvider {

	/**
	 * Default <code>Map</code> of <code>ICSSPropertyHandler</code> stored
	 * under a CSS property <code>name</code> key.
	 */
	private static Map<String, Class<? extends ICSSPropertyHandler>> defaultCSSProperties = new HashMap<String, Class<? extends ICSSPropertyHandler>>();

	private Map<Class<?>, List<ICSSPropertyHandler>> propertiesHandler = new HashMap<Class<?>, List<ICSSPropertyHandler>>();

	/**
	 * Custom <code>Map</code> of <code>ICSSPropertyHandler</code> stored
	 * under a CSS property <code>name</code> key.
	 */
	private Map<String, Class<? extends ICSSPropertyHandler>> customCSSProperties = new HashMap<String, Class<? extends ICSSPropertyHandler>>();

	/**
	 * True if custom CSS properties is merged with default CSS Properties.
	 */
	private boolean isCSSPropertiesInitialized;

	static {
		// http://www.w3schools.com/css/css_reference.asp
		// Register CSS2 properties

		// Register CSS2 Background Properties
		registerDefaultCSSProperty("background",
				ICSSPropertyBackgroundHandler.class);
		registerDefaultCSSProperty("background-attachment",
				ICSSPropertyBackgroundHandler.class);
		registerDefaultCSSProperty("background-color",
				ICSSPropertyBackgroundHandler.class);
		registerDefaultCSSProperty("background-image",
				ICSSPropertyBackgroundHandler.class);
		registerDefaultCSSProperty("background-position",
				ICSSPropertyBackgroundHandler.class);
		registerDefaultCSSProperty("background-repeat",
				ICSSPropertyBackgroundHandler.class);

		// Register CSS2 Border Properties
		registerDefaultCSSProperty("border", ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-bottom",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-bottom-color",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-bottom-style",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-bottom-width",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-color",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-left",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-left-color",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-left-style",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-left-width",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-right",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-right-color",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-right-style",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-right-width",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-style",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-top",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-top-color",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-top-style",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-top-width",
				ICSSPropertyBorderHandler.class);
		registerDefaultCSSProperty("border-width",
				ICSSPropertyBorderHandler.class);

		// Register CSS2 Classification Properties
		registerDefaultCSSProperty("clear",
				ICSSPropertyClassificationHandler.class);
		registerDefaultCSSProperty("cursor",
				ICSSPropertyClassificationHandler.class);
		registerDefaultCSSProperty("display",
				ICSSPropertyClassificationHandler.class);
		registerDefaultCSSProperty("float",
				ICSSPropertyClassificationHandler.class);
		registerDefaultCSSProperty("position",
				ICSSPropertyClassificationHandler.class);
		registerDefaultCSSProperty("visibility",
				ICSSPropertyClassificationHandler.class);

		// Register CSS2 Dimension Properties
		registerDefaultCSSProperty("height", ICSSPropertyDimensionHandler.class);
		registerDefaultCSSProperty("line-height",
				ICSSPropertyDimensionHandler.class);
		registerDefaultCSSProperty("max-height",
				ICSSPropertyDimensionHandler.class);
		registerDefaultCSSProperty("max-width",
				ICSSPropertyDimensionHandler.class);
		registerDefaultCSSProperty("min-height",
				ICSSPropertyDimensionHandler.class);
		registerDefaultCSSProperty("min-width",
				ICSSPropertyDimensionHandler.class);
		registerDefaultCSSProperty("width", ICSSPropertyDimensionHandler.class);

		// Register CSS2 Font Properties
		registerDefaultCSSProperty("font", ICSSPropertyFontHandler.class);
		registerDefaultCSSProperty("font-family", ICSSPropertyFontHandler.class);
		registerDefaultCSSProperty("font-size", ICSSPropertyFontHandler.class);
		registerDefaultCSSProperty("font-size-adjust",
				ICSSPropertyFontHandler.class);
		registerDefaultCSSProperty("font-stretch",
				ICSSPropertyFontHandler.class);
		registerDefaultCSSProperty("font-style", ICSSPropertyFontHandler.class);
		registerDefaultCSSProperty("font-variant",
				ICSSPropertyFontHandler.class);
		registerDefaultCSSProperty("font-weight", ICSSPropertyFontHandler.class);

		// Register CSS2 Text Properties
		registerDefaultCSSProperty("color", ICSSPropertyTextHandler.class);
		registerDefaultCSSProperty("text-transform",
				ICSSPropertyTextHandler.class);

		// Register Margin Properties
		registerDefaultCSSProperty("margin", ICSSPropertyMarginHandler.class);
		registerDefaultCSSProperty("margin-bottom",
				ICSSPropertyMarginHandler.class);
		registerDefaultCSSProperty("margin-left",
				ICSSPropertyMarginHandler.class);
		registerDefaultCSSProperty("margin-top",
				ICSSPropertyMarginHandler.class);
		registerDefaultCSSProperty("margin-right",
				ICSSPropertyMarginHandler.class);
		
		// Register Padding Properties
		registerDefaultCSSProperty("padding", ICSSPropertyPaddingHandler.class);
		registerDefaultCSSProperty("padding-bottom",
				ICSSPropertyPaddingHandler.class);
		registerDefaultCSSProperty("padding-left",
				ICSSPropertyPaddingHandler.class);
		registerDefaultCSSProperty("padding-top",
				ICSSPropertyPaddingHandler.class);
		registerDefaultCSSProperty("padding-right",
				ICSSPropertyPaddingHandler.class);
	}

	public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
			String property) throws Exception {
		Class<? extends ICSSPropertyHandler> cl = getCSSPropertyHandlerClass(property);
		return propertiesHandler.get(cl);
	}

	/*--------------- CSS Property Handler -----------------*/

	public void registerCSSPropertyHandler(Class<?> cl,
			ICSSPropertyHandler handler) {
		List<ICSSPropertyHandler> handlers = propertiesHandler.get(cl);
		if (handlers == null) {
			propertiesHandler.put(cl,
					handlers = new LinkedList<ICSSPropertyHandler>());
		}
		handlers.add(handler);
	}

	protected Class<? extends ICSSPropertyHandler> getCSSPropertyHandlerClass(
			String property) {
		initializeCSSPropertiesIfNeed();
		return customCSSProperties.get(property);

	}

	public void registerCSSProperty(String propertyName,
			Class<? extends ICSSPropertyHandler> propertyHandlerClass) {
		customCSSProperties.put(propertyName, propertyHandlerClass);
	}

	public static void registerDefaultCSSProperty(String propertyName,
			Class<? extends ICSSPropertyHandler> propertyHandlerClass) {
		defaultCSSProperties.put(propertyName, propertyHandlerClass);
	}

	/**
	 * Merge custom CSS Properties with default CSS properties.
	 */
	private void initializeCSSPropertiesIfNeed() {
		if (isCSSPropertiesInitialized)
			return;
		customCSSProperties.putAll(defaultCSSProperties);
		isCSSPropertiesInitialized = true;
	}

	public Collection<String> getAllCSSPropertyNames() {
		initializeCSSPropertiesIfNeed();
		return customCSSProperties.keySet();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.dom.properties.providers.AbstractCSSPropertyHandlerProvider#getDefaultCSSStyleDeclaration(org.eclipse.e4.ui.css.core.engine.CSSEngine,
	 *      org.eclipse.e4.ui.css.core.dom.CSSStylableElement,
	 *      org.w3c.dom.css.CSSStyleDeclaration, java.lang.String)
	 */
	public CSSStyleDeclaration getDefaultCSSStyleDeclaration(CSSEngine engine,
			CSSStylableElement stylableElement, CSSStyleDeclaration newStyle,
			String pseudoE) throws Exception {
		CSSStyleDeclaration defaultStyleDeclaration = stylableElement
				.getDefaultStyleDeclaration(pseudoE);
		if (defaultStyleDeclaration != null)
			// default style is already computed for the stylable element ,
			// return it.
			return defaultStyleDeclaration;

		// Default style must be computed.
		StringBuffer style = null;
		Collection<String> propertyNames = getAllCSSPropertyNames();
		for (String propertyName : propertyNames) {
			String s = getCSSPropertyStyle(engine, stylableElement,
					propertyName, pseudoE);
			if (s != null) {
				if (style == null)
					style = new StringBuffer();
				style.append(s);
			}
		}
		if (style != null) {
			defaultStyleDeclaration = engine.parseStyleDeclaration(style
					.toString());
			stylableElement.setDefaultStyleDeclaration(pseudoE,
					defaultStyleDeclaration);
			return defaultStyleDeclaration;
		}
		return null;
	}

	public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
			Object element, String property) throws Exception {
		return getCSSPropertyHandlers(property);
	}

	public Collection<String> getCSSProperties(Object element) {
		return getAllCSSPropertyNames();
	}

}
