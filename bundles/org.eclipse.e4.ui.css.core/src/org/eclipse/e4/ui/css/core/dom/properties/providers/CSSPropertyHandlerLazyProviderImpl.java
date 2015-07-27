/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *     Red Hat Inc. (mistria) - Fixes suggested by FindBugs
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.e4.ui.css.core.dom.CSSStylableElement;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.exceptions.UnsupportedClassCSSPropertyException;
import org.eclipse.e4.ui.css.core.utils.StringUtils;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * CSS property handler with lazy strategy. {@link ICSSPropertyHandler} are
 * retrieved with name into packages registered with registerPackage method.
 */
public class CSSPropertyHandlerLazyProviderImpl extends
		AbstractCSSPropertyHandlerProvider {

	// List of package names containing handlers class for properties
	private List<String> packageNames = new ArrayList<String>();

	// Map used as a cache for properties handlers found
	private Map<String, List<ICSSPropertyHandler>> propertyToHandlersMap = new HashMap<String, List<ICSSPropertyHandler>>();

	/**
	 * Return the list of PropertiesHandler corresponding to the property name
	 * given as argument
	 */
	@Override
	public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
			String property) throws Exception {

		// Test if ICSSPropertyHandler List was stored into cache
		// with key property
		Map<String, List<ICSSPropertyHandler>> propertyHandlers = getPropertyToHandlersMap();
		if (propertyHandlers.containsKey(property)) {
			return propertyHandlers.get(property);
		}

		List<ICSSPropertyHandler> handlers = null;
		try {
			String handlerClassName = getHandlerClassName(property);
			for (String packageName : packageNames) {
				ICSSPropertyHandler handler = getCSSPropertyHandler(
						packageName, handlerClassName);
				if (handler != null) {
					//TODO replace with eclipse logging
//					if (logger.isDebugEnabled())
//						logger.debug("Handle CSS Property=" + property
//								+ ", with class=" + packageName + "."
//								+ handlerClassName);
					if (handlers == null)
						handlers = new ArrayList<ICSSPropertyHandler>();
					handlers.add(handler);
				}
			}
			//TODO replace with eclipse logging
//			if (logger.isDebugEnabled()) {
//				if (handlers == null)
//					logger.debug("Cannot find Handle Class CSS Property="
//							+ property + ", for class=" + handlerClassName);
//			}
		} finally {
			propertyHandlers.put(property, handlers);
		}
		return handlers;
	}

	/**
	 * Register a package path "name.name1." where to search for PropertyHandler
	 * class
	 *
	 * @param packageName
	 */
	public void registerPackage(String packageName) {
		packageNames.add(packageName);
		propertyToHandlersMap = null;
	}

	protected Map<String, List<ICSSPropertyHandler>> getPropertyToHandlersMap() {
		if (propertyToHandlersMap == null)
			propertyToHandlersMap = new HashMap<String, List<ICSSPropertyHandler>>();
		return propertyToHandlersMap;
	}

	/**
	 * Reflexive method that return a property handler class
	 *
	 * @param packageName
	 * @param handlerClassName
	 * @return
	 * @throws Exception
	 */
	protected ICSSPropertyHandler getCSSPropertyHandler(String packageName,
			String handlerClassName) throws Exception {
		String handlerClass = packageName + "." + handlerClassName;
		try {
			Class<?> clazz = this.getClass().getClassLoader()
					.loadClass(
					handlerClass);
			Object instance = clazz.newInstance();
			if (!(instance instanceof ICSSPropertyHandler)) {
				throw new UnsupportedClassCSSPropertyException(clazz);
			}
			return (ICSSPropertyHandler) clazz.newInstance();
		} catch (ClassNotFoundException e) {

		}
		return null;
	}

	/**
	 * Return the handler class name corresponding to the property label given
	 * as argument A Property Handler Class Name is CSSPropertyXXXHandler (like
	 * CSSPropertyBorderTopColorHandler)
	 *
	 * @param property
	 * @return
	 */
	protected String getHandlerClassName(String property) {
		StringBuilder handlerClassName = new StringBuilder("CSSProperty"); //$NON-NLS-1$
		String[] s = StringUtils.split(property, "-"); //$NON-NLS-1$
		for (int i = 0; i < s.length; i++) {
			String p = s[i];
			handlerClassName.append(p.substring(0, 1).toUpperCase());
			handlerClassName.append(p.substring(1));
		}
		handlerClassName.append("Handler"); //$NON-NLS-1$
		return handlerClassName.toString();
	}

	@Override
	protected CSSStyleDeclaration getDefaultCSSStyleDeclaration(
			CSSEngine engine, CSSStylableElement stylableElement,
			CSSStyleDeclaration newStyle, String pseudoE) throws Exception {
		if (stylableElement.getDefaultStyleDeclaration(pseudoE) != null)
			return stylableElement.getDefaultStyleDeclaration(pseudoE);
		if (newStyle != null) {
			StringBuffer style = null;
			int length = newStyle.getLength();
			for (int i = 0; i < length; i++) {
				String propertyName = newStyle.item(i);
				String[] compositePropertiesNames = engine
						.getCSSCompositePropertiesNames(propertyName);
				if (compositePropertiesNames != null) {
					for (int j = 0; j < compositePropertiesNames.length; j++) {
						propertyName = compositePropertiesNames[j];
						String s = getCSSPropertyStyle(engine, stylableElement,
								propertyName, pseudoE);
						if (s != null) {
							if (style == null)
								style = new StringBuffer();
							style.append(s);
						}
					}
				} else {
					String s = getCSSPropertyStyle(engine, stylableElement,
							propertyName, pseudoE);
					if (s != null) {
						if (style == null)
							style = new StringBuffer();
						style.append(s);
					}
				}
			}
			if (style != null) {
				CSSStyleDeclaration defaultStyleDeclaration = engine
						.parseStyleDeclaration(style.toString());
				stylableElement.setDefaultStyleDeclaration(pseudoE,
						defaultStyleDeclaration);
				return defaultStyleDeclaration;
			}
		}
		return stylableElement.getDefaultStyleDeclaration(pseudoE);
	}

	@Override
	public Collection<ICSSPropertyHandler> getCSSPropertyHandlers(
			Object element, String property) throws Exception {
		return getCSSPropertyHandlers(property);
	}

	@Override
	public Collection<String> getCSSProperties(Object element) {
		Map<String, List<ICSSPropertyHandler>> propertyHandlers = getPropertyToHandlersMap();
		// FIXME: could walk the package names, look for the classes matching
		// the class pattern and go from there
		return propertyHandlers.keySet();
	}

}
